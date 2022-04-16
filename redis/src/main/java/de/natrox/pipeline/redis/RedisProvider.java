package de.natrox.pipeline.redis;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.cache.GlobalCacheProvider;
import de.natrox.pipeline.part.updater.DataUpdater;
import de.natrox.pipeline.part.updater.DataUpdaterProvider;
import org.jetbrains.annotations.NotNull;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.misc.RedisURI;

import java.util.concurrent.TimeUnit;

public final class RedisProvider implements DataUpdaterProvider, GlobalCacheProvider {

    private final RedissonClient redissonClient;

    protected RedisProvider(@NotNull RedisConfig config) throws Exception {
        Preconditions.checkNotNull(config, "config");

        var endpoints = config.endpoints();
        var size = endpoints.size();
        if (size == 0)
            throw new IllegalArgumentException("Endpoints Array is empty");

        var redisConfig = new Config();
        if (size > 1) {
            var clusterServersConfig = redisConfig.useClusterServers();

            for (var endpoint : endpoints) {
                var uri = new RedisURI("redis", endpoint.host(), endpoint.port());
                clusterServersConfig.addNodeAddress(uri.toString());
            }

            if (!Strings.isNullOrEmpty(config.username()))
                clusterServersConfig.setUsername(config.username());
            if (!Strings.isNullOrEmpty(config.password()))
                clusterServersConfig.setPassword(config.password());

        } else {
            var endpoint = endpoints.get(0);
            var singleServerConfig = redisConfig.useSingleServer();
            var uri = new RedisURI("redis", endpoint.host(), endpoint.port());
            singleServerConfig
                .setSubscriptionsPerConnection(30)
                .setAddress(uri.toString())
                .setDatabase(endpoint.database());

            if (!Strings.isNullOrEmpty(config.username()))
                singleServerConfig.setUsername(config.username());
            if (!Strings.isNullOrEmpty(config.password()))
                singleServerConfig.setPassword(config.password());
        }

        redisConfig.setNettyThreads(4);
        redisConfig.setThreads(4);
        this.redissonClient = Redisson.create(redisConfig);
    }

    @Override
    public void shutdown() {
        redissonClient.shutdown(0, 2, TimeUnit.SECONDS);
    }

    @Override
    public DataUpdater constructDataUpdater(Pipeline pipeline) {
        return new RedisDataUpdater(pipeline, redissonClient);
    }

    @Override
    public GlobalCache constructGlobalCache(Pipeline pipeline) {
        return new RedisCache(pipeline, redissonClient);
    }
}
