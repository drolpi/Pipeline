/*
 * Copyright 2020-2022 NatroxMC team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.natrox.pipeline.redis;

import com.google.common.base.Strings;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.provider.GlobalCacheProvider;
import de.natrox.pipeline.part.Store;
import org.jetbrains.annotations.NotNull;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.misc.RedisURI;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class RedisProvider implements GlobalCacheProvider {

    private final RedissonClient redissonClient;

    RedisProvider(@NotNull RedisConfig config) {
        Check.notNull(config, "config");

        List<RedisEndpoint> endpoints = config.endpoints();
        int size = endpoints.size();
        if (size == 0)
            throw new IllegalArgumentException("Endpoints Array is empty");

        Config redisConfig = new Config();
        if (size > 1) {
            ClusterServersConfig clusterServersConfig = redisConfig.useClusterServers();

            for (RedisEndpoint endpoint : endpoints) {
                RedisURI uri = new RedisURI("redis", endpoint.host(), endpoint.port());
                clusterServersConfig.addNodeAddress(uri.toString());
            }

            if (!Strings.isNullOrEmpty(config.username()))
                clusterServersConfig.setUsername(config.username());
            if (!Strings.isNullOrEmpty(config.password()))
                clusterServersConfig.setPassword(config.password());

        } else {
            RedisEndpoint endpoint = endpoints.get(0);
            SingleServerConfig singleServerConfig = redisConfig.useSingleServer();
            RedisURI uri = new RedisURI("redis", endpoint.host(), endpoint.port());
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
    public void close() {
        this.redissonClient.shutdown(0, 2, TimeUnit.SECONDS);
    }

    @Override
    public @NotNull Store createGlobalCache(@NotNull Pipeline pipeline) {
        return new RedisCache(pipeline, this.redissonClient);
    }
}
