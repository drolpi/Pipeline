/*
 * Copyright 2020-2022 NatroxMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.natrox.pipeline.document;

import de.natrox.common.container.Pair;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.condition.Condition;
import de.natrox.pipeline.document.find.FindOptions;
import de.natrox.pipeline.document.option.DocumentOptions;
import de.natrox.pipeline.mapper.DocumentMapper;
import de.natrox.pipeline.part.Store;
import de.natrox.pipeline.part.StoreMap;
import de.natrox.pipeline.part.connecting.ConnectingStore;
import de.natrox.pipeline.repository.Cursor;
import de.natrox.pipeline.sort.SortEntry;
import de.natrox.pipeline.sort.SortOrder;
import de.natrox.pipeline.stream.BoundedStream;
import de.natrox.pipeline.stream.ConditionalStream;
import de.natrox.pipeline.stream.DocumentStream;
import de.natrox.pipeline.stream.PipeStream;
import de.natrox.pipeline.stream.SortedDocumentStream;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

final class DocumentRepositoryImpl implements DocumentRepository {

    private final String repositoryName;
    private final DocumentMapper documentMapper;
    private final DocumentOptions options;

    private Store store;
    private StoreMap storeMap;

    DocumentRepositoryImpl(String repositoryName, Pipeline pipeline, ConnectingStore store, StoreMap storeMap, DocumentOptions options) {
        this.repositoryName = repositoryName;
        this.documentMapper = pipeline.documentMapper();
        this.options = options;
        this.store = store;
        this.storeMap = storeMap;
    }

    @Override
    public @NotNull Optional<DocumentData> get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        this.checkOpened();

        byte[] data = this.storeMap.get(uniqueId);
        if (data == null)
            return Optional.empty();
        return Optional.of(this.documentMapper.read(data));
    }

    @Override
    public @NotNull Cursor<DocumentData> find(@NotNull FindOptions findOptions) {
        Check.notNull(findOptions, "findOptions");
        this.checkOpened();

        PipeStream<Pair<UUID, DocumentData>> stream = PipeStream.fromIterable(
            this.storeMap
                .entries()
                .entrySet()
                .stream()
                .map(entry -> Pair.of(entry.getKey(), this.documentMapper.read(entry.getValue())))
                .collect(Collectors.toList())
        );

        Condition condition = findOptions.condition();
        if (condition != null) {
            stream = new ConditionalStream(condition, stream);
        }

        SortEntry sortBy = findOptions.sortBy();
        if (sortBy != null) {
            List<Pair<String, SortOrder>> blockingSortOrder = sortBy.sortingOrders();
            if (!blockingSortOrder.isEmpty()) {
                stream = new SortedDocumentStream(blockingSortOrder, stream);
            }
        }

        if (findOptions.limit() != -1 || findOptions.skip() != -1) {
            long limit = findOptions.limit() == -1 ? Long.MAX_VALUE : findOptions.limit();
            long skip = findOptions.skip() == -1 ? 0 : findOptions.skip();
            stream = new BoundedStream<>(skip, limit, stream);
        }

        return new DocumentStream(stream);
    }

    @Override
    public void insert(@NotNull UUID uniqueId, @NotNull DocumentData document) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(document, "document");
        this.checkOpened();

        DocumentData newDoc = document.clone();
        newDoc.append(DocumentDataImpl.DOC_ID, uniqueId);

        this.storeMap.put(uniqueId, this.documentMapper.write(newDoc));
    }

    @Override
    public boolean exists(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        this.checkOpened();
        return this.storeMap.contains(uniqueId);
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        this.checkOpened();
        this.storeMap.remove(uniqueId);
    }

    @Override
    public @NotNull String name() {
        return this.repositoryName;
    }

    @Override
    public void clear() {
        this.checkOpened();
        this.storeMap.clear();
    }

    @Override
    public void close() {
        this.store.closeMap(this.repositoryName);

        this.store = null;
        this.storeMap = null;
    }

    @Override
    public boolean isDropped() {
        return !this.store.hasMap(this.repositoryName);
    }


    @Override
    public void drop() {
        this.checkOpened();
        this.close();

        this.store.removeMap(this.repositoryName);
    }

    @Override
    public boolean isOpen() {
        return this.store != null && !this.store.isClosed() && !this.isDropped();
    }

    @Override
    public long size() {
        this.checkOpened();
        return this.storeMap.size();
    }

    private void checkOpened() {
        if (this.isOpen())
            return;
        throw new RuntimeException("Repository is closed");
    }
}
