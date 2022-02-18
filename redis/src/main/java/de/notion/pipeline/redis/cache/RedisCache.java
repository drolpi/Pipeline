package de.notion.pipeline.redis.cache;

import de.notion.pipeline.annotation.auto.AutoCleanUp;
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
            return getObjectCache(dataClass, objectUUID).get();
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
        RBucket<String> objectCache = getObjectCache(dataClass, objectUUID);
        objectCache.set(dataToSave);

        //Update the expire time again because after setting new data the expire time resets
        updateExpireTime(dataClass, objectCache);
    }

    @Override
    public synchronized boolean removeData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        RBucket<String> bucket = getObjectCache(dataClass, objectUUID);
        return bucket.delete();
    }

    public synchronized RBucket<String> getObjectCache(Class<? extends PipelineData> dataClass, UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        RBucket<String> objectCache = redissonClient.getBucket("Cache:" + AnnotationResolver.getStorageIdentifier(dataClass) + ":" + objectUUID, new StringCodec());
        updateExpireTime(dataClass, objectCache);
        return objectCache;
    }

    public synchronized Set<String> getKeys(Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        String mongoIdentifier = AnnotationResolver.getStorageIdentifier(dataClass);
        return redissonClient.getKeys().getKeysStream().filter(s -> {
            String[] parts = s.split(":");
            return parts[1].equals(mongoIdentifier);
        }).collect(Collectors.toSet());
    }

    @Override
    public synchronized Set<UUID> getSavedUUIDs(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        return getKeys(dataClass).stream().map(s -> UUID.fromString(s.split(":")[2])).collect(Collectors.toSet());
    }

    @Override
    public synchronized boolean dataExist(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        RBucket<String> cache = getObjectCache(dataClass, objectUUID);

        return cache.isExists();
    }

    private void updateExpireTime(@NotNull Class<? extends PipelineData> dataClass, RBucket<?> bucket) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        AutoCleanUp autoCleanUp = AnnotationResolver.getAutoCleanUp(dataClass);

        if (bucket == null)
            return;

        if (autoCleanUp == null) {
            bucket.expire(12, TimeUnit.HOURS);
        } else {
            bucket.expire(autoCleanUp.time(), autoCleanUp.timeUnit());
        }
    }

    @Override
    public void updateExpireTime(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        updateExpireTime(dataClass, getObjectCache(dataClass, objectUUID));
    }
}
