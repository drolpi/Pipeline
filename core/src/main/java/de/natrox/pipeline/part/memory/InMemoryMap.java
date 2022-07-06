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

package de.natrox.pipeline.part.memory;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.store.StoreMap;
import de.natrox.pipeline.repository.QueryStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class InMemoryMap implements StoreMap {

    private final Map<UUID, byte[]> map;

    InMemoryMap() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public byte @Nullable [] get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        return this.map.get(uniqueId);
    }

    @Override
    public void put(@NotNull UUID uniqueId, byte @NotNull [] documentData) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(documentData, "documentData");
        this.map.put(uniqueId, documentData);
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        return this.map.containsKey(uniqueId);
    }

    @Override
    public @NotNull Collection<UUID> keys() {
        return this.map.keySet();
    }

    @Override
    public @NotNull Collection<byte[]> values() {
        return this.map.values();
    }

    @Override
    public @NotNull Map<UUID, byte[]> entries() {
        return this.map;
    }

    @Override
    public void remove(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        this.map.remove(uniqueId);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public long size() {
        return this.map.size();
    }
}
