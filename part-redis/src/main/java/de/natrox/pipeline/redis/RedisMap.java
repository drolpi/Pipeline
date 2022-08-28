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

package de.natrox.pipeline.redis;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.config.GlobalCacheConfig;
import de.natrox.pipeline.part.store.StoreMap;
import de.natrox.pipeline.repository.QueryStrategy;
import de.natrox.pipeline.repository.RepositoryOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

final class RedisMap implements StoreMap {

    private final RedisStore redisStore;
    private final RedissonClient redissonClient;
    private final String mapName;
    private final boolean storageMode;
    private final long expireAfterAccessNanos;
    private final long expireAfterWriteNanos;

    RedisMap(RedisStore redisStore, String mapName, boolean storageMode, RepositoryOptions options) {
        this.redisStore = redisStore;
        this.redissonClient = redisStore.redissonClient();
        this.mapName = mapName;
        this.storageMode = storageMode;

        if (!options.useGlobalCache()) {
            this.expireAfterAccessNanos = -1;
            this.expireAfterWriteNanos = -1;
            return;
        }

        GlobalCacheConfig config = options.globalCacheConfig();
        this.expireAfterAccessNanos = config.expireAfterAccessNanos();
        this.expireAfterWriteNanos = config.expireAfterWriteNanos();
    }

    @Override
    public @Nullable Object get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        RBucket<Object> bucket = this.bucket(uniqueId);
        if (!bucket.isExists())
            return null;

        this.updateExpireTime(bucket, this.expireAfterAccessNanos);
        return bucket.get();
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull Object data, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(data, "data");

        RBucket<Object> bucket = this.bucket(uniqueId);
        bucket.set(data);
        this.updateExpireTime(bucket, this.expireAfterWriteNanos);
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        RBucket<Object> bucket = this.bucket(uniqueId);
        this.updateExpireTime(bucket, this.expireAfterAccessNanos);
        return bucket.isExists();
    }

    @Override
    public @NotNull Collection<UUID> keys() {
        //TODO: Update expire
        return this.redisStore.keys(this.mapName)
            .stream()
            .map(s -> UUID.fromString(s.split(":")[2]))
            .collect(Collectors.toList());
    }

    @Override
    public @NotNull Collection<Object> values() {
        //TODO: Update expire
        Set<String> keys = this.redisStore.keys(this.mapName);

        List<Object> documents = new ArrayList<>();
        for (var key : keys) {
            RBucket<Object> bucket = this.redissonClient.getBucket(key);

            if (!bucket.isExists())
                continue;

            Object data = bucket.get();
            if (data == null)
                continue;

            documents.add(data);
        }
        return documents;
    }

    @Override
    public @NotNull Map<UUID, Object> entries() {
        //TODO: Update expire
        Collection<UUID> keys = this.keys();
        Map<UUID, Object> entries = new HashMap<>();

        for (var key : keys) {
            RBucket<Object> bucket = this.bucket(key);

            if (!bucket.isExists())
                continue;

            Object data = bucket.get();
            if (data == null)
                continue;

            entries.put(key, data);
        }
        return entries;
    }

    @Override
    public void remove(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        RBucket<Object> bucket = this.bucket(uniqueId);
        bucket.delete();
    }

    @Override
    public void clear() {
        Set<String> keys = this.redisStore.keys(this.mapName);
        RKeys redisKeys = this.redissonClient.getKeys();
        redisKeys.delete(keys.toArray(new String[0]));
    }

    @Override
    public long size() {
        return this.redisStore.keys(this.mapName).size();
    }

    private RBucket<Object> bucket(UUID uniqueId) {
        return this.redissonClient.getBucket("Cache:" + this.mapName + ":" + uniqueId.toString());
    }

    private void updateExpireTime(RBucket<Object> bucket, long nanos) {
        Check.notNull(bucket, "bucket");
        Check.notNull(nanos, "nanos");
        if (this.storageMode || nanos < 0)
            return;

        bucket.expireAsync(Duration.of(nanos, ChronoUnit.NANOS));
    }
}
