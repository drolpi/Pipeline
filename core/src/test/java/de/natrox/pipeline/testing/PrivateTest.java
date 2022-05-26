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

package de.natrox.pipeline.testing;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.mongo.MongoConfig;
import de.natrox.pipeline.mongo.MongoProvider;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.object.ObjectRepository;
import de.natrox.pipeline.object.annotation.Properties;
import de.natrox.pipeline.part.memory.InMemoryProvider;
import de.natrox.pipeline.redis.RedisConfig;
import de.natrox.pipeline.redis.RedisProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PrivateTest {

    private final static UUID ID = UUID.nameUUIDFromBytes("Test".getBytes(StandardCharsets.UTF_8));

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
            .endpoint(builder -> builder.host("127.0.0.1").port(6379).database(0))
            .build();
        RedisProvider redisProvider = redisConfig.createProvider();
        InMemoryProvider inMemoryProvider = InMemoryProvider.create();

        Pipeline pipeline1 = Pipeline
            .of(mongoProvider, redisProvider, inMemoryProvider, redisProvider)
            .build();

        Pipeline pipeline2 = Pipeline
            .of(mongoProvider, redisProvider, inMemoryProvider, redisProvider)
            .build();

        {
            ObjectRepository<AccountData> repository = pipeline1.repository(AccountData.class);
            AccountData accountData = repository.loadOrCreate(ID);
            accountData.setName("Lars");
            repository.save(accountData);
        }

        {
            ObjectRepository<AccountData> repository = pipeline2.repository(AccountData.class);
            repository.load(ID);
        }

        {
            ObjectRepository<AccountData> repository = pipeline1.repository(AccountData.class);
            repository.load(ID).ifPresent(accountData -> {
                accountData.setName("Ida");
                repository.save(accountData);
            });
        }

    }

    @Properties(identifier = "AccData")
    static class AccountData extends ObjectData {

        private String name;

        public AccountData(Pipeline pipeline) {
            super(pipeline);
        }

        @Override
        public void handleCreate() {
            System.out.println("Create");
        }

        @Override
        public void handleUpdate(@NotNull DocumentData before) {
            System.out.println("N: " + this.name + " B: " + before.get("name", String.class));
        }

        public String name() {
            return this.name;
        }

        public AccountData setName(String name) {
            this.name = name;
            return this;
        }
    }

}
