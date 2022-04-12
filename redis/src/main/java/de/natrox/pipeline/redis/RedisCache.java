package de.natrox.pipeline.redis;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.part.cache.GlobalCache;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

final class RedisCache implements GlobalCache {

    private final static Logger LOGGER = LogManager.logger(RedisCache.class);

    private final Gson gson;
    private final RedissonClient redissonClient;

    protected RedisCache(Pipeline pipeline, RedissonClient redissonClient) {
        this.gson = pipeline.gson();
        this.redissonClient = redissonClient;

        LOGGER.debug("Redis cache initialized");
    }

    @Override
    public synchronized JsonObject loadData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        try {
            return JsonParser.parseString(objectCache(dataClass, objectUUID).get()).getAsJsonObject();
        } catch (Exception e) {
            LOGGER.severe("Error while loading " + dataClass + " with uuid " + objectUUID + " -> removing ...");
            removeData(dataClass, objectUUID);
        }
        return null;
    }

    @Override
    public synchronized boolean dataExist(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        var cache = objectCache(dataClass, objectUUID);
        return cache.isExists();
    }

    @Override
    public synchronized void saveData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonObject data) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        Preconditions.checkNotNull(data, "data");

        var objectCache = objectCache(dataClass, objectUUID);
        objectCache.set(gson.toJson(data));

        //Update the expire time again because after setting new data the expire time resets
        updateExpireTime(dataClass, objectCache);
    }

    @Override
    public synchronized boolean removeData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        var bucket = objectCache(dataClass, objectUUID);
        return bucket.delete();
    }

    @Override
    public synchronized @NotNull List<UUID> savedUUIDs(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        return keys(dataClass).stream().map(s -> UUID.fromString(s.split(":")[2])).collect(Collectors.toList());
    }

    @Override
    public void updateExpireTime(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        updateExpireTime(dataClass, objectCache(dataClass, objectUUID));
    }

    private void updateExpireTime(@NotNull Class<? extends PipelineData> dataClass, RBucket<?> bucket) {
        Preconditions.checkNotNull(dataClass, "dataClass");
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
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        RBucket<String> objectCache = redissonClient.getBucket("Cache:" + AnnotationResolver.storageIdentifier(dataClass) + ":" + objectUUID, new StringCodec());
        updateExpireTime(dataClass, objectCache);
        return objectCache;
    }

    public synchronized Set<String> keys(Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        var redisIdentifier = AnnotationResolver.storageIdentifier(dataClass);
        return redissonClient
            .getKeys()
            .getKeysStream()
            .filter(s -> s.split(":")[1].equals(redisIdentifier))
            .collect(Collectors.toSet());
    }
}
