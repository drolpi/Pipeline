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

import de.natrox.common.validate.Check;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.mapper.DocumentMapper;
import de.natrox.pipeline.part.AbstractStore;
import de.natrox.pipeline.part.StoreMap;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RedissonClient;

import java.util.Set;
import java.util.stream.Collectors;

final class RedisStore extends AbstractStore {

    private final DocumentMapper documentMapper;
    private final RedissonClient redissonClient;

    RedisStore(Pipeline pipeline, RedissonClient redissonClient) {
        this.documentMapper = pipeline.documentMapper();
        this.redissonClient = redissonClient;
    }

    @Override
    protected StoreMap createMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        return new RedisMap(this, mapName, this.documentMapper);
    }

    @Override
    public @NotNull Set<String> maps() {
        return this.redissonClient
            .getKeys()
            .getKeysStream()
            .filter(this::filterKey)
            .map(s -> s.split(":")[1])
            .collect(Collectors.toSet());
    }

    @Override
    public boolean hasMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        return true;
    }

    @Override
    public void removeMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        this.redissonClient.getKeys().delete(this.keys(mapName).toArray(new String[0]));
        this.storeMapRegistry.remove(mapName);
    }

    Set<String> keys(String mapName) {
        return this.redissonClient
            .getKeys()
            .getKeysStream()
            .filter(this::filterKey)
            .collect(Collectors.toSet());
    }

    private boolean filterKey(String mapName) {
        String[] split = mapName.split(":");
        if (split.length != 3)
            return false;

        return split[1].equals(mapName);
    }

    public RedissonClient redissonClient() {
        return this.redissonClient;
    }
}
