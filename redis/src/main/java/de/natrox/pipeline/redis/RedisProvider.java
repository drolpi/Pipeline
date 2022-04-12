package de.natrox.pipeline.redis;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.cache.GlobalCacheProvider;
import de.natrox.pipeline.part.updater.DataUpdater;
import de.natrox.pipeline.part.updater.DataUpdaterProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.misc.RedisURI;

import java.util.concurrent.TimeUnit;

public final class RedisProvider implements DataUpdaterProvider, GlobalCacheProvider {

    private final RedisConfig config;
    private @Nullable RedissonClient redissonClient;

    protected RedisProvider(@NotNull RedisConfig config) {
        Preconditions.checkNotNull(config, "config");
        this.config = config;
    }

    @Override
    public boolean init() throws Exception {
        var endpoints = config.endpoints();
        var size = endpoints.size();
        if (size == 0)
            throw new IllegalArgumentException("Endpoints Array is empty");

        var config = new Config();
        if (size > 1) {
            var clusterServersConfig = config.useClusterServers();

            for (var endpoint : endpoints) {
                var uri = new RedisURI("redis", endpoint.host(), endpoint.port());
                clusterServersConfig.addNodeAddress(uri.toString());
            }

            if (!Strings.isNullOrEmpty(this.config.username()))
                clusterServersConfig.setUsername(this.config.username());
            if (!Strings.isNullOrEmpty(this.config.password()))
                clusterServersConfig.setPassword(this.config.password());

        } else {
            var endpoint = endpoints.get(0);
            var singleServerConfig = config.useSingleServer();
            var uri = new RedisURI("redis", endpoint.host(), endpoint.port());
            singleServerConfig
                .setSubscriptionsPerConnection(30)
                .setAddress(uri.toString())
                .setDatabase(endpoint.database());

            if (!Strings.isNullOrEmpty(this.config.username()))
                singleServerConfig.setUsername(this.config.username());
            if (!Strings.isNullOrEmpty(this.config.password()))
                singleServerConfig.setPassword(this.config.password());
        }

        config.setNettyThreads(4);
        config.setThreads(4);
        this.redissonClient = Redisson.create(config);
        return true;
    }

    @Override
    public void shutdown() {
        if (redissonClient != null) {
            redissonClient.shutdown(0, 2, TimeUnit.SECONDS);
        }
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
