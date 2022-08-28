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
import com.github.benmanes.caffeine.cache.Scheduler;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.config.LocalCacheConfig;
import de.natrox.pipeline.part.store.StoreMap;
import de.natrox.pipeline.repository.QueryStrategy;
import de.natrox.pipeline.repository.RepositoryOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

final class CaffeineMap implements StoreMap {

    private final Cache<UUID, Object> cache;

    CaffeineMap(RepositoryOptions options) {
        LocalCacheConfig config = options.localCacheConfig();
        Caffeine<Object, Object> builder = Caffeine
            .newBuilder()
            .scheduler(Scheduler.systemScheduler());

        long expireAfterWriteNanos = config.expireAfterWriteNanos();
        if (expireAfterWriteNanos >= 0)
            builder.expireAfterWrite(config.expireAfterWriteNanos(), TimeUnit.NANOSECONDS);

        long expireAfterAccessNanos = config.expireAfterAccessNanos();
        if (expireAfterAccessNanos >= 0)
            builder.expireAfterAccess(config.expireAfterAccessNanos(), TimeUnit.NANOSECONDS);

        this.cache = builder.build();
    }

    @Override
    public @Nullable Object get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        return this.cache.getIfPresent(uniqueId);
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull Object data, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(data, "data");
        this.cache.put(uniqueId, data);
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        return this.cache.asMap().containsKey(uniqueId);
    }

    @Override
    public @NotNull Collection<UUID> keys() {
        return this.cache.asMap().keySet();
    }

    @Override
    public @NotNull Collection<Object> values() {
        return this.cache.asMap().values();
    }

    @Override
    public @NotNull Map<UUID, Object> entries() {
        return this.cache.asMap();
    }

    @Override
    public void remove(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
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
