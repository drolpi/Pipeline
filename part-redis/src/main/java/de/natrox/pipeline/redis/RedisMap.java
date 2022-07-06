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
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RBinaryStream;
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
    private final GlobalCacheConfig config;

    RedisMap(RedisStore redisStore, String mapName, GlobalCacheConfig config) {
        this.redisStore = redisStore;
        this.redissonClient = redisStore.redissonClient();
        this.mapName = mapName;
        this.config = config;
    }

    @Override
    public byte[] get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        RBinaryStream stream = this.stream(uniqueId);
        if (!stream.isExists())
            return null;

        this.updateExpireTime(stream, this.config.expireAfterAccessNanos());
        return stream.get();
    }

    @Override
    public void put(@NotNull UUID uniqueId, byte @NotNull [] data) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(data, "data");

        RBinaryStream stream = this.stream(uniqueId);
        stream.set(data);
        this.updateExpireTime(stream, this.config.expireAfterWriteNanos());
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        RBinaryStream stream = this.stream(uniqueId);
        this.updateExpireTime(stream, this.config.expireAfterAccessNanos());
        return stream.isExists();
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
    public @NotNull Collection<byte[]> values() {
        //TODO: Update expire
        Set<String> keys = this.redisStore.keys(this.mapName);

        List<byte[]> documents = new ArrayList<>();
        for (var key : keys) {
            RBinaryStream stream = this.redissonClient.getBinaryStream(key);

            if (!stream.isExists())
                continue;

            byte[] bytes = stream.get();
            if (bytes == null)
                continue;

            documents.add(bytes);
        }
        return documents;
    }

    @Override
    public @NotNull Map<UUID, byte[]> entries() {
        //TODO: Update expire
        Collection<UUID> keys = this.keys();
        Map<UUID, byte[]> entries = new HashMap<>();

        for (var key : keys) {
            RBinaryStream stream = this.stream(key);

            if (!stream.isExists())
                continue;

            byte[] bytes = stream.get();
            if (bytes == null)
                continue;

            entries.put(key, bytes);
        }
        return entries;
    }

    @Override
    public void remove(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        RBinaryStream stream = this.stream(uniqueId);
        stream.delete();
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

    private RBinaryStream stream(UUID uniqueId) {
        return this.redissonClient.getBinaryStream("Cache:" + this.mapName + ":" + uniqueId.toString());
    }

    private void updateExpireTime(RBinaryStream stream, long nanos) {
        Check.notNull(stream, "stream");
        Check.notNull(nanos, "nanos");
        if(nanos < 0)
            return;

        stream.expireAsync(Duration.of(nanos, ChronoUnit.NANOS));
    }
}
