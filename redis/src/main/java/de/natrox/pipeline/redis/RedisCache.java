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

import de.natrox.common.validate.Check;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.part.cache.GlobalCache;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

final class RedisCache implements GlobalCache {

    private final static Logger LOGGER = LoggerFactory.getLogger(RedisCache.class);

    private final JsonDocument.Factory documentFactory;
    private final RedissonClient redissonClient;

    protected RedisCache(Pipeline pipeline, RedissonClient redissonClient) {
        this.documentFactory = pipeline.documentFactory();
        this.redissonClient = redissonClient;

        LOGGER.debug("Redis cache initialized");
    }

    @Override
    public synchronized JsonDocument get(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(objectUUID, "objectUUID");

        var bucket = objectCache(dataClass, objectUUID);

        try {
            return documentFactory.fromJsonString(bucket.get());
        } catch (Exception e) {
            LOGGER.error("Error while loading " + dataClass + " with uuid " + objectUUID + " -> removing ...");
            remove(dataClass, objectUUID);
        }
        return null;
    }

    @Override
    public synchronized boolean exists(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(objectUUID, "objectUUID");

        var cache = objectCache(dataClass, objectUUID);
        return cache.isExists();
    }

    @Override
    public synchronized void save(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonDocument data) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(objectUUID, "objectUUID");
        Check.notNull(data, "data");

        var bucket = objectCache(dataClass, objectUUID);
        bucket.set(data.toString());

        //Update the expire time again because after setting new data the expire time resets
        updateExpireTime(dataClass, bucket);
    }

    @Override
    public synchronized boolean remove(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(objectUUID, "objectUUID");

        var bucket = objectCache(dataClass, objectUUID);
        return bucket.delete();
    }

    @Override
    public synchronized @NotNull List<UUID> keys(@NotNull Class<? extends PipelineData> dataClass) {
        Check.notNull(dataClass, "dataClass");

        return redisKeys(dataClass)
            .stream()
            .map(s -> UUID.fromString(s.split(":")[2]))
            .collect(Collectors.toList());
    }

    @Override
    public @NotNull Collection<JsonDocument> documents(@NotNull Class<? extends PipelineData> dataClass) {
        Check.notNull(dataClass, "dataClass");

        var buckets = redissonClient.getBuckets();
        var keys = redisKeys(dataClass);
        var map = buckets.get(redisKeys(dataClass).toArray(new String[0]));

        Collection<JsonDocument> documents = new ArrayList<>();
        for (var entry : map.entrySet()) {
            var objectValue = entry.getValue();

            if (!(objectValue instanceof String stringValue))
                continue;

            documents.add(documentFactory.fromJsonString(stringValue));
        }
        return documents;
    }

    @Override
    public @NotNull Map<UUID, JsonDocument> entries(@NotNull Class<? extends PipelineData> dataClass) {
        Check.notNull(dataClass, "dataClass");
        return filter(dataClass, (uuid, strings) -> true);
    }

    @Override
    public @NotNull Map<UUID, JsonDocument> filter(@NotNull Class<? extends PipelineData> dataClass, @NotNull BiPredicate<UUID, JsonDocument> predicate) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(predicate, "predicate");

        var buckets = redissonClient.getBuckets(StringCodec.INSTANCE);
        var keys = redisKeys(dataClass);
        var map = buckets.get(keys.toArray(new String[0]));

        Map<UUID, JsonDocument> entries = new HashMap<>();
        for (var entry : map.entrySet()) {
            var key = UUID.fromString(entry.getKey().split(":")[2]);
            var objectValue = entry.getValue();

            if (!(objectValue instanceof String stringValue))
                continue;

            var value = documentFactory.fromJsonString(stringValue);

            if (predicate.test(key, value)) {
                entries.put(key, value);
            }
        }
        return entries;
    }

    @Override
    public void iterate(@NotNull Class<? extends PipelineData> dataClass, @NotNull BiConsumer<UUID, JsonDocument> consumer) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(consumer, "consumer");
        this.entries(dataClass).forEach(consumer);
    }

    @Override
    public void updateExpireTime(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(objectUUID, "objectUUID");

        updateExpireTime(dataClass, objectCache(dataClass, objectUUID));
    }

    private void updateExpireTime(@NotNull Class<? extends PipelineData> dataClass, RBucket<?> bucket) {
        Check.notNull(dataClass, "dataClass");
        var optional = AnnotationResolver.cleanUp(dataClass);
        if (bucket == null)
            return;

        if (optional.isEmpty()) {
            bucket.expireAsync(Duration.ofHours(12));
        } else {
            var autoCleanUp = optional.get();
            bucket.expireAsync(Duration.of(autoCleanUp.time(), autoCleanUp.timeUnit()));
        }
    }

    public synchronized RBucket<String> objectCache(Class<? extends PipelineData> dataClass, UUID objectUUID) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(objectUUID, "objectUUID");

        RBucket<String> objectCache = redissonClient.getBucket("Cache:" + AnnotationResolver.storageIdentifier(dataClass) + ":" + objectUUID, StringCodec.INSTANCE);
        updateExpireTime(dataClass, objectCache);
        return objectCache;
    }

    public synchronized Set<String> redisKeys(Class<? extends PipelineData> dataClass) {
        Check.notNull(dataClass, "dataClass");
        var redisIdentifier = AnnotationResolver.storageIdentifier(dataClass);
        return redissonClient
            .getKeys()
            .getKeysStream()
            .filter(s -> s.split(":")[1].equals(redisIdentifier))
            .collect(Collectors.toSet());
    }
}
