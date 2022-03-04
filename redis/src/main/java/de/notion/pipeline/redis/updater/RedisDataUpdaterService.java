package de.notion.pipeline.redis.updater;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.config.PipelineRegistry;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.local.updater.DataUpdater;
import de.notion.pipeline.part.local.updater.DataUpdaterService;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RedissonClient;

import java.util.HashMap;
import java.util.Map;

public class RedisDataUpdaterService implements DataUpdaterService {

    private final Pipeline pipeline;
    private final PipelineRegistry registry;
    private final RedissonClient redissonClient;
    private final Map<Class<? extends PipelineData>, DataUpdater> cache;

    public RedisDataUpdaterService(Pipeline pipeline, RedissonClient redissonClient) {
        this.pipeline = pipeline;
        this.registry = pipeline.registry();
        this.redissonClient = redissonClient;
        this.cache = new HashMap<>();
        this.registerClasses();
        System.out.println("Redis data updater service started");
    }

    private void registerClasses() {
        for (Class<? extends PipelineData> dataClass : registry.dataClasses())
            cache.put(dataClass, new RedisDataUpdater(pipeline, redissonClient, dataClass));
    }

    @Override
    public DataUpdater dataUpdater(@NotNull Class<? extends PipelineData> dataClass) {
        if (!cache.containsKey(dataClass))
            cache.put(dataClass, new RedisDataUpdater(pipeline, redissonClient, dataClass));
        return cache.get(dataClass);
    }
}
