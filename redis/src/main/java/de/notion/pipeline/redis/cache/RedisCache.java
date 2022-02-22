package de.notion.pipeline.redis.cache;

import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.cache.GlobalCache;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RedisCache implements GlobalCache {

    private final RedissonClient redissonClient;

    public RedisCache(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        System.out.println("Redis Cache started");
    }

    @Override
    public synchronized String loadData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        try {
            return objectCache(dataClass, objectUUID).get();
        } catch (Exception e) {
            System.out.println("Error while loading " + dataClass + " with uuid " + objectUUID + " -> removing ...");
            removeData(dataClass, objectUUID);
            return null;
        }
    }

    @Override
    public synchronized void saveData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull String dataToSave) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        var objectCache = objectCache(dataClass, objectUUID);
        objectCache.set(dataToSave);

        //Update the expire time again because after setting new data the expire time resets
        updateExpireTime(dataClass, objectCache);
    }

    @Override
    public synchronized boolean removeData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        var bucket = objectCache(dataClass, objectUUID);
        return bucket.delete();
    }

    @Override
    public synchronized Set<UUID> savedUUIDs(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        return keys(dataClass).stream().map(s -> UUID.fromString(s.split(":")[2])).collect(Collectors.toSet());
    }

    @Override
    public synchronized boolean dataExist(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        var cache = objectCache(dataClass, objectUUID);

        return cache.isExists();
    }

    @Override
    public void updateExpireTime(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        updateExpireTime(dataClass, objectCache(dataClass, objectUUID));
    }

    private void updateExpireTime(@NotNull Class<? extends PipelineData> dataClass, RBucket<?> bucket) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        var optional = AnnotationResolver.cleanUp(dataClass);

        if (bucket == null)
            return;

        if (!optional.isPresent()) {
            bucket.expire(12, TimeUnit.HOURS);
        } else {
            var autoCleanUp = optional.get();
            bucket.expire(autoCleanUp.time(), autoCleanUp.timeUnit());
        }
    }

    public synchronized RBucket<String> objectCache(Class<? extends PipelineData> dataClass, UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        RBucket<String> objectCache = redissonClient.getBucket("Cache:" + AnnotationResolver.storageIdentifier(dataClass) + ":" + objectUUID, new StringCodec());
        updateExpireTime(dataClass, objectCache);
        return objectCache;
    }

    public synchronized Set<String> keys(Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        var mongoIdentifier = AnnotationResolver.storageIdentifier(dataClass);
        return redissonClient.getKeys().getKeysStream().filter(s -> {
            var parts = s.split(":");
            return parts[1].equals(mongoIdentifier);
        }).collect(Collectors.toSet());
    }
}
