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

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.part.PartMap;
import de.natrox.pipeline.part.cache.GlobalCache;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

final class RedisCache implements GlobalCache {

    private final JsonConverter jsonConverter;
    private final RedissonClient redissonClient;
    private final Map<String, RedisMap> redisMapRegistry;

    RedisCache(Pipeline pipeline, RedissonClient redissonClient) {
        this.jsonConverter = pipeline.jsonConverter();
        this.redissonClient = redissonClient;
        this.redisMapRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public @NotNull PartMap openMap(@NotNull String mapName) {
        if (redisMapRegistry.containsKey(mapName)) {
            return redisMapRegistry.get(mapName);
        }
        RedisMap redisMap = new RedisMap(redissonClient, mapName, jsonConverter);
        redisMapRegistry.put(mapName, redisMap);

        return redisMap;
    }

    @Override
    public boolean hasMap(String mapName) {
        long keys = redissonClient
            .getKeys()
            .getKeysStream()
            .filter(s -> s.split(":")[1].equals(mapName))
            .count();

        return keys > 0;
    }

    @Override
    public void closeMap(String mapName) {
        redisMapRegistry.remove(mapName);
    }

    @Override
    public void removeMap(String mapName) {
        redissonClient.getKeys().delete(keys(mapName).toArray(new String[0]));
        redisMapRegistry.remove(mapName);
    }

    private Set<String> keys(String mapName) {
        return redissonClient
            .getKeys()
            .getKeysStream()
            .filter(s -> s.split(":")[1].equals(mapName))
            .collect(Collectors.toSet());
    }
}
