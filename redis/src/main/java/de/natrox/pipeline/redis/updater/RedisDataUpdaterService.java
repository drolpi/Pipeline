package de.natrox.pipeline.redis.updater;

import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.part.updater.DataUpdater;
import de.natrox.pipeline.part.updater.DataUpdaterService;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RedissonClient;

import java.util.HashMap;
import java.util.Map;

public class RedisDataUpdaterService implements DataUpdaterService {

    private final static Logger LOGGER = LogManager.logger(RedisDataUpdaterService.class);

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
        //LOGGER.info("Redis data updater service started"); //DEBUG
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
