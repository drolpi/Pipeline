/*
 * Copyright 2020-2022 NatroxMC
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

import de.natrox.pipeline.repository.Pipeline;
import de.natrox.pipeline.part.config.GlobalCacheConfig;
import de.natrox.pipeline.part.store.Store;
import de.natrox.pipeline.part.updater.Updater;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("ClassCanBeRecord")
final class RedisProviderImpl implements RedisProvider {

    private final RedissonClient redissonClient;

    RedisProviderImpl(@NotNull RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void close() {
        this.redissonClient.shutdown(0, 2, TimeUnit.SECONDS);
    }

    @Override
    public @NotNull Updater createDataUpdater(@NotNull Pipeline pipeline) {
        return new RedisUpdater(this.redissonClient);
    }

    @Override
    public @NotNull Store createGlobalCache(@NotNull Pipeline pipeline, @NotNull GlobalCacheConfig config) {
        return new RedisStore(this.redissonClient);
    }
}