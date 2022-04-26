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
import de.natrox.pipeline.part.map.PartMap;
import de.natrox.pipeline.sort.SortOrder;
import de.natrox.pipeline.stream.BoundedStream;
import de.natrox.pipeline.stream.ConditionStream;
import de.natrox.pipeline.stream.DocumentStream;
import de.natrox.pipeline.stream.PipeStream;
import de.natrox.pipeline.stream.SortedDocumentStream;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DocumentRepositoryImpl implements DocumentRepository {

    private final String repositoryName;
    private final PartMap partMap;

    public DocumentRepositoryImpl(String repositoryName, PartMap partMap) {
        this.repositoryName = repositoryName;
        this.partMap = partMap;
    }

    @Override
    public @NotNull Optional<PipeDocument> get(@NotNull UUID uniqueId) {
        return Optional.ofNullable(partMap.get(uniqueId));
    }

    @Override
    public @NotNull DocumentCursor find(@NotNull Condition condition, @NotNull FindOption findOption) {
        //TODO: Maybe allow that condition is null and use PartMap#entries direct if condition is null
        PipeStream<Pair<UUID, PipeDocument>> stream = new ConditionStream(partMap.entries(), condition);

        if (findOption.orderBy() != null) {
            List<Pair<String, SortOrder>> blockingSortOrder = findOption.orderBy().getSortingOrders();
            if (!blockingSortOrder.isEmpty()) {
                stream = new SortedDocumentStream(blockingSortOrder, stream);
            }
        }

        if (findOption.limit() != null || findOption.skip() != null) {
            long limit = findOption.limit() == null ? Long.MAX_VALUE : findOption.limit();
            long skip = findOption.skip() == null ? 0 : findOption.skip();
            stream = new BoundedStream<>(skip, limit, stream);
        }

        return new DocumentStream(stream);
    }

    @Override
    public void insert(@NotNull UUID uniqueId, @NotNull PipeDocument document) {
        PipeDocument newDoc = document.clone();

        //TODO:

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
    public String name() {
        return repositoryName;
    }

    @Override
    public void drop() {

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

    @Override
    public void close() {

    }
}
