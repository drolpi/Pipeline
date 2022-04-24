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
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.map.PartMap;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    public PartMap openMap(String mapName) {
        if (redisMapRegistry.containsKey(mapName)) {
            return redisMapRegistry.get(mapName);
        }
        RedisMap redisMap = new RedisMap(redissonClient, mapName, jsonConverter);
        redisMapRegistry.put(mapName, redisMap);

        return redisMap;
    }
}
