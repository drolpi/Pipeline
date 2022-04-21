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

package de.natrox.pipeline.test;

import de.natrox.pipeline.redis.RedisConfig;
import de.natrox.pipeline.redis.RedisEndpoint;
import de.natrox.pipeline.redis.RedisProvider;

public class RedisTest {

    public static void main(String[] args) throws Exception {
RedisConfig redisConfig = RedisConfig
    .builder()
    .endpoints(
        RedisEndpoint
            .builder()
            .host("0.0.0.0")
            .port(6379)
            .database(0) //optional
            .build(),
        RedisEndpoint
            .builder()
            .host("0.0.0.1")
            .port(6379)
            .database(5) //optional
            .build()
    )
    .username("username") //optional
    .password("password") //optional
    .build();
        RedisProvider redisProvider = redisConfig.createProvider();
    }

}
