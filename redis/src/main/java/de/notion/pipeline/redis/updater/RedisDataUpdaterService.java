package de.notion.pipeline.redis.updater;

import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.local.LocalCache;
import de.notion.pipeline.part.local.updater.DataUpdater;
import de.notion.pipeline.part.local.updater.DataUpdaterService;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RedissonClient;

import java.util.HashMap;
import java.util.Map;

public class RedisDataUpdaterService implements DataUpdaterService {

    private final RedissonClient redissonClient;
    private final LocalCache localCache;
    private final Map<Class<? extends PipelineData>, DataUpdater> cache;

    public RedisDataUpdaterService(RedissonClient redissonClient, LocalCache localCache) {
        this.redissonClient = redissonClient;
        this.localCache = localCache;
        this.cache = new HashMap<>();
        System.out.println("Redis DataUpdaterService started");
    }

    @Override
    public DataUpdater dataUpdater(@NotNull Class<? extends PipelineData> dataClass) {
        cache.putIfAbsent(dataClass, new RedisDataUpdater(redissonClient, localCache, dataClass));
        return cache.get(dataClass);
    }
}
