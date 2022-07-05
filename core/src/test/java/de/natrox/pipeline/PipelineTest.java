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

package de.natrox.pipeline;

import de.natrox.pipeline.caffeine.CaffeineProvider;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.mongo.MongoConfig;
import de.natrox.pipeline.mongo.MongoProvider;
import de.natrox.pipeline.redis.RedisConfig;
import de.natrox.pipeline.redis.RedisProvider;
import de.natrox.pipeline.repository.DocumentRepository;
import de.natrox.pipeline.repository.Pipeline;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

class PipelineTest {

    @Test
    void test() {
        MongoConfig mongoConfig = MongoConfig
            .create()
            .setHost("127.0.0.1")
            .setPort(27017)
            .setDatabase("local");
        MongoProvider mongoProvider = MongoProvider.of(mongoConfig);

        RedisConfig redisConfig = RedisConfig
            .create()
            .addEndpoint(endpoint -> endpoint.setHost("127.0.0.1").setPort(6379).setDatabase(0));
        RedisProvider redisProvider = RedisProvider.of(redisConfig);

        CaffeineProvider caffeineProvider = CaffeineProvider.create();

        Pipeline pipeline = Pipeline
            .create(mongoProvider, builder -> builder)
            .globalCache(redisProvider, builder -> builder)
            .localCache(caffeineProvider, redisProvider, builder -> builder.expireAfterWrite(10, TimeUnit.SECONDS))
            .build();

        {
            DocumentRepository repository = pipeline
                .buildRepository("OnlineTime")
                .useGlobalCache(true)
                .useLocalCache(true)
                .build();

            UUID uuid = UUID.randomUUID();
            Instant start = Instant.now();
            repository.insert(uuid, DocumentData.create("test", "test"));
            System.out.println(Duration.between(start, Instant.now()).toMillis());

            Instant middle = Instant.now();
            repository.get(uuid);
            System.out.println(Duration.between(middle, Instant.now()).toMillis());
        }
    }

}
