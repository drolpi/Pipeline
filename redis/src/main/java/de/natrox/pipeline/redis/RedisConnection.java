package de.natrox.pipeline.redis;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.config.connection.Connection;
import de.natrox.pipeline.config.connection.DataUpdaterConnection;
import de.natrox.pipeline.config.connection.GlobalCacheConnection;
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.updater.DataUpdaterService;
import de.natrox.pipeline.redis.cache.RedisCache;
import de.natrox.pipeline.redis.updater.RedisDataUpdaterService;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class RedisConnection implements DataUpdaterConnection, GlobalCacheConnection, Connection {

    private final boolean clusterMode;
    private final String[] addresses;
    private final String password;

    private RedissonClient redissonClient;
    private boolean connected;

    public RedisConnection(boolean useCluster, String password, String... addresses) {
        this.clusterMode = useCluster;
        this.addresses = addresses;
        this.password = password;
        this.connected = false;
    }

    @Override
    public void load() {
        if (isLoaded())
            return;
        if (addresses.length == 0)
            throw new IllegalArgumentException("Address Array empty");

        var config = new Config();
        if (clusterMode) {
            var clusterServersConfig = config.useClusterServers();
            clusterServersConfig.addNodeAddress(addresses);

            if (!password.isEmpty())
                clusterServersConfig.addNodeAddress(addresses).setPassword(password);
            else
                clusterServersConfig.addNodeAddress(addresses);
        } else {
            var address = addresses[0];
            var singleServerConfig = config.useSingleServer();
            singleServerConfig.setSubscriptionsPerConnection(30);

            if (!password.isEmpty())
                singleServerConfig.setAddress(address).setPassword(password);
            else
                singleServerConfig.setAddress(address);
        }
        config.setNettyThreads(4);
        config.setThreads(4);
        this.redissonClient = Redisson.create(config);
        connected = true;
    }

    @Override
    public void shutdown() {
        redissonClient.shutdown(0, 2, TimeUnit.SECONDS);
        connected = false;
    }

    @Override
    public boolean isLoaded() {
        return connected;
    }

    @Override
    public DataUpdaterService constructDataUpdaterService(Pipeline pipeline) {
        return new RedisDataUpdaterService(pipeline, redissonClient);
    }

    @Override
    public GlobalCache constructGlobalCache(Pipeline pipeline) {
        return new RedisCache(pipeline, redissonClient);
    }
}
