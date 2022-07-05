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

package de.natrox.pipeline.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.config.LocalCacheConfig;
import de.natrox.pipeline.part.store.StoreMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

final class CaffeineMap implements StoreMap {

    private final Cache<UUID, byte[]> cache;

    CaffeineMap(LocalCacheConfig config) {
        this.cache = Caffeine
            .newBuilder()
            .build();
    }

    @Override
    public byte @Nullable [] get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        return this.cache.getIfPresent(uniqueId);
    }

    @Override
    public void put(@NotNull UUID uniqueId, byte @NotNull [] documentData) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(documentData, "documentData");
        this.cache.put(uniqueId, documentData);
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        return this.cache.asMap().containsKey(uniqueId);
    }

    @Override
    public @NotNull Collection<UUID> keys() {
        return this.cache.asMap().keySet();
    }

    @Override
    public @NotNull Collection<byte[]> values() {
        return this.cache.asMap().values();
    }

    @Override
    public @NotNull Map<UUID, byte[]> entries() {
        return this.cache.asMap();
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        this.cache.invalidate(uniqueId);
    }

    @Override
    public void clear() {
        this.cache.invalidateAll();
    }

    @Override
    public long size() {
        return this.cache.asMap().size();
    }
}
