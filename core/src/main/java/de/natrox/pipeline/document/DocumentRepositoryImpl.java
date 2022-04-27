/*
 * Copyright 2020-2022 NatroxMC team
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
import de.natrox.pipeline.condition.Condition;
import de.natrox.pipeline.document.find.FindOptions;
import de.natrox.pipeline.part.Part;
import de.natrox.pipeline.part.map.PartMap;
import de.natrox.pipeline.sort.SortOrder;
import de.natrox.pipeline.sort.SortableFields;
import de.natrox.pipeline.stream.BoundedStream;
import de.natrox.pipeline.stream.ConditionStream;
import de.natrox.pipeline.stream.DocumentStream;
import de.natrox.pipeline.stream.PipeStream;
import de.natrox.pipeline.stream.SortedDocumentStream;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public final class DocumentRepositoryImpl implements DocumentRepository {

    private final String repositoryName;
    private final Part part;
    private final PartMap partMap;

    public DocumentRepositoryImpl(String repositoryName, Part part, PartMap partMap) {
        this.repositoryName = repositoryName;
        this.part = part;
        this.partMap = partMap;
    }

    @Override
    public @NotNull Optional<PipeDocument> get(@NotNull UUID uniqueId) {
        return Optional.ofNullable(partMap.get(uniqueId));
    }

    @Override
    public @NotNull DocumentCursor find(@NotNull FindOptions findOptions) {
        PipeStream<Pair<UUID, PipeDocument>> stream = partMap.entries();

        Condition condition = findOptions.condition();
        if (condition != null) {
            stream = new ConditionStream(condition, stream);
        }

        SortableFields sortBy = findOptions.sortBy();
        if (sortBy != null) {
            List<Pair<String, SortOrder>> blockingSortOrder = sortBy.getSortingOrders();
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
    public void insert(@NotNull UUID uniqueId, @NotNull PipeDocument document) {
        PipeDocument newDoc = document.clone();

        newDoc.put(PipeDocumentImpl.DOC_ID, uniqueId);

        partMap.put(uniqueId, newDoc);
    }

    @Override
    public boolean exists(@NotNull UUID uniqueId) {
        return partMap.contains(uniqueId);
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        partMap.remove(uniqueId);
    }

    @Override
    public @NotNull String name() {
        return repositoryName;
    }

    @Override
    public void close() {
        part.removeMap(repositoryName);
    }

    @Override
    public void drop() {
        part.closeMap(repositoryName);
        part.removeMap(repositoryName);
    }

    @Override
    public boolean isDropped() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public long size() {
        return 0;
    }
}
