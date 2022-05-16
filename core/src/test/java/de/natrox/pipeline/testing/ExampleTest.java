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

package de.natrox.pipeline.testing;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.condition.Conditions;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.document.DocumentRepository;
import de.natrox.pipeline.document.find.FindOptions;
import de.natrox.pipeline.jackson.JacksonConverter;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.mongo.MongoConfig;
import de.natrox.pipeline.mongo.MongoProvider;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.object.ObjectRepository;
import de.natrox.pipeline.object.annotation.Properties;
import de.natrox.pipeline.part.memory.InMemoryProvider;
import de.natrox.pipeline.redis.RedisConfig;
import de.natrox.pipeline.redis.RedisEndpoint;
import de.natrox.pipeline.redis.RedisProvider;
import de.natrox.pipeline.repository.Cursor;
import de.natrox.pipeline.sort.Sorts;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ExampleTest {

    @Test
    public void test() {
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
        InMemoryProvider inMemoryProvider = InMemoryProvider.create();

        JsonConverter jsonConverter = JacksonConverter.create();

        Pipeline pipeline = Pipeline
            .of(mongoProvider, redisProvider, inMemoryProvider, redisProvider)
            .jsonConverter(jsonConverter)
            .build();

        // Document repository
        {
            DocumentRepository repository = pipeline.repository("DAccount");
            UUID uniqueId = UUID.randomUUID();

            // Insert
            {
                ThreadLocalRandom random = ThreadLocalRandom.current();

                DocumentData documentData = DocumentData
                    .create()
                    .append("name", random.nextInt(0, 2) == 0 ? "Aaron" : "Zaher")
                    .append("age", random.nextInt(0, 100))
                    .append("european", random.nextBoolean());

                repository.insert(uniqueId, documentData);
            }

            // Get
            {
                DocumentData documentData = repository.get(uniqueId).orElse(null);
            }

            // Remove
            {
                repository.remove(uniqueId);
            }

            // Find
            {
                Cursor<DocumentData> cursor = repository.find(
                    FindOptions
                        .builder()
                        .condition(Conditions.and(Conditions.eq("european", true), Conditions.gt("age", 18)))
                        .sort(Sorts.and(Sorts.ascending("name"), Sorts.descending("age")))
                        .build()
                );

                for (DocumentData documentData : cursor) {

                }

                DocumentData documentData = cursor.first().orElse(null);
            }
        }

        // Object repository
        {
            ObjectRepository<AccountData> repository = pipeline.repository(AccountData.class);
            UUID uniqueId = UUID.randomUUID();

            // Load
            {
                AccountData accountData = repository.load(uniqueId).orElse(null);
            }

            // Remove
            {
                repository.remove(uniqueId);
            }

            // Find
            {
                Cursor<AccountData> cursor = repository.find(
                    FindOptions
                        .builder()
                        .condition(Conditions.and(Conditions.eq("european", true), Conditions.gt("age", 18)))
                        .sort(Sorts.and(Sorts.ascending("name"), Sorts.descending("age")))
                        .build()
                );

                for (AccountData accountData : cursor) {

                }

                AccountData accountData = cursor.first().orElse(null);
            }
        }
    }

    @Properties(identifier = "OAccount")
    static class AccountData extends ObjectData {

        private String name;
        private int age;
        private boolean european;

        public AccountData(Pipeline pipeline) {
            super(pipeline);
        }

        public String name() {
            return this.name;
        }

        public AccountData setName(String name) {
            this.name = name;
            return this;
        }

        public int age() {
            return this.age;
        }

        public AccountData setAge(int age) {
            this.age = age;
            return this;
        }

        public boolean european() {
            return this.european;
        }

        public AccountData setEuropean(boolean european) {
            this.european = european;
            return this;
        }
    }

}
