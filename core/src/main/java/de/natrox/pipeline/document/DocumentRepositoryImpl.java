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

import de.natrox.pipeline.filter.Filter;
import de.natrox.pipeline.part.map.PartMap;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull DocumentCursor find(@NotNull Filter filter, @NotNull FindOptions findOptions) {
        return null;
    }

    @Override
    public void insert(@NotNull UUID uniqueId, @NotNull PipeDocument document) {
        partMap.put(uniqueId, document);
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
