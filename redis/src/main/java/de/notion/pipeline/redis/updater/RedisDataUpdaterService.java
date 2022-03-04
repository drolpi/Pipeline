package de.notion.pipeline.redis.updater;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.local.LocalCache;
import de.notion.pipeline.part.local.updater.DataUpdater;
import de.notion.pipeline.part.local.updater.DataUpdaterService;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RedissonClient;

import java.util.HashMap;
import java.util.Map;

public class RedisDataUpdaterService implements DataUpdaterService {

    private final LocalCache localCache;
    private final RedissonClient redissonClient;
    private final Map<Class<? extends PipelineData>, DataUpdater> cache;

    public RedisDataUpdaterService(Pipeline pipeline, RedissonClient redissonClient) {
        this.localCache = pipeline.localCache();
        this.redissonClient = redissonClient;
        this.cache = new HashMap<>();
        System.out.println("Redis DataUpdaterService started");
    }

    @Override
    public DataUpdater dataUpdater(@NotNull Class<? extends PipelineData> dataClass) {
        if (!cache.containsKey(dataClass))
            cache.put(dataClass, new RedisDataUpdater(redissonClient, localCache, dataClass));
        return cache.get(dataClass);
    }
}
