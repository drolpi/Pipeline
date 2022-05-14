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

package de.natrox.pipeline.redis;

import de.natrox.common.container.Pair;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.part.PartMap;
import de.natrox.pipeline.stream.PipeStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RBucket;
import org.redisson.api.RBuckets;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("ClassCanBeRecord")
final class RedisMap implements PartMap {

    //TODO: Add null checks

    private final RedissonClient redissonClient;
    private final String mapName;
    private final JsonConverter jsonConverter;

    RedisMap(RedissonClient redissonClient, String mapName, JsonConverter jsonConverter) {
        this.redissonClient = redissonClient;
        this.mapName = mapName;
        this.jsonConverter = jsonConverter;
    }

    @Override
    public @Nullable DocumentData get(@NotNull UUID uniqueId) {
        RBucket<String> bucket = this.bucket(uniqueId);
        String json = bucket.get();
        if (json == null)
            return null;

        return this.jsonConverter.fromJson(json, DocumentData.class);
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull DocumentData documentData) {
        RBucket<String> bucket = this.bucket(uniqueId);
        bucket.set(this.jsonConverter.toJson(documentData));
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        RBucket<String> bucket = this.bucket(uniqueId);
        return bucket.isExists();
    }

    @Override
    public @NotNull PipeStream<UUID> keys() {
        List<UUID> keys = redisKeys()
            .stream()
            .map(s -> UUID.fromString(s.split(":")[2]))
            .collect(Collectors.toList());
        return PipeStream.fromIterable(keys);
    }

    @Override
    public @NotNull PipeStream<DocumentData> values() {
        Set<String> keys = this.redisKeys();
        RBuckets redisBuckets = this.redissonClient.getBuckets();
        Map<String, Object> buckets = redisBuckets.get(keys.toArray(new String[0]));

        List<DocumentData> documents = new ArrayList<>();
        for (var entry : buckets.entrySet()) {
            Object objectValue = entry.getValue();

            if (!(objectValue instanceof String stringValue))
                continue;

            documents.add(this.jsonConverter.fromJson(stringValue, DocumentData.class));
        }
        return PipeStream.fromIterable(documents);
    }

    @Override
    public @NotNull PipeStream<Pair<UUID, DocumentData>> entries() {
        Set<String> keys = this.redisKeys();
        RBuckets redisBuckets = this.redissonClient.getBuckets();
        Map<String, Object> buckets = redisBuckets.get(keys.toArray(new String[0]));

        Map<UUID, DocumentData> entries = new HashMap<>();
        for (var entry : buckets.entrySet()) {
            UUID key = UUID.fromString(entry.getKey().split(":")[2]);
            Object objectValue = entry.getValue();

            if (!(objectValue instanceof String stringValue))
                continue;

            DocumentData value = this.jsonConverter.fromJson(stringValue, DocumentData.class);

            entries.put(key, value);
        }
        return PipeStream.fromMap(entries);
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        RBucket<String> bucket = this.bucket(uniqueId);
        bucket.delete();
    }

    @Override
    public void clear() {

    }

    @Override
    public long size() {
        return 0;
    }

    private RBucket<String> bucket(UUID uniqueId) {
        return this.redissonClient.getBucket("Cache:" + this.mapName + ":" + uniqueId, StringCodec.INSTANCE);
    }

    public synchronized Set<String> redisKeys() {
        return this.redissonClient
            .getKeys()
            .getKeysStream()
            .filter(s -> s.split(":")[1].equals(this.mapName))
            .collect(Collectors.toSet());
    }
}
