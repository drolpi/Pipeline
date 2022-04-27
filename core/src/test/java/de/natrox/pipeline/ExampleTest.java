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

package de.natrox.pipeline;

import de.natrox.pipeline.document.DocumentRepository;
import de.natrox.pipeline.jackson.JacksonConverter;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.mongo.MongoConfig;
import de.natrox.pipeline.mongo.MongoProvider;
import de.natrox.pipeline.redis.RedisConfig;
import de.natrox.pipeline.redis.RedisEndpoint;
import de.natrox.pipeline.redis.RedisProvider;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class ExampleTest {

    @Test
    public void test() throws Exception {
        MongoConfig mongoConfig = MongoConfig
            .builder()
            .host("127.0.0.1")
            .port(27017)
            .database("rewrite")
            .build();
        MongoProvider mongoProvider = mongoConfig.createProvider();

        RedisConfig redisConfig = RedisConfig
            .builder()
            .endpoints(
                RedisEndpoint
                    .builder()
                    .host("127.0.0.1")
                    .port(6379)
                    .database(0)
                    .build()
            )
            .build();
        RedisProvider redisProvider = redisConfig.createProvider();

        PartBundle bundle = PartBundle.global(mongoProvider, redisProvider);
        JsonConverter jsonConverter = JacksonConverter.create();

        Pipeline pipeline = Pipeline
            .builder()
            .bundle(bundle)
            .jsonConverter(jsonConverter)
            .build();

        DocumentRepository repository = pipeline.repository("Test");
        repository.get(UUID.fromString("5c4c6e43-422e-4a2f-9b98-2786faa53442"));

        repository.drop();
        //ThreadLocalRandom random = ThreadLocalRandom.current();
        //
        //for (int i = 0; i < 3000; i++) {
        //    PipeDocument document = PipeDocument
        //        .create()
        //        .put("name", random.nextInt(0, 2) == 0 ? "Aaron" : "Zaher")
        //        .put("age", random.nextInt(0, 100))
        //        .put("hobby", "test");
        //    repository.insert(UUID.randomUUID(), document);
        //}

        var instant = Instant.now();

        /*
        repository.get(UUID.fromString("5c4c6e43-422e-4a2f-9b98-2786faa53442")).ifPresent(document -> {
            System.out.println(document.toString());
        });
         */

        System.out.println(Duration.between(instant, Instant.now()).toMillis());
    }

    static class TestObject {

        private final String name = "asd";

    }

}
