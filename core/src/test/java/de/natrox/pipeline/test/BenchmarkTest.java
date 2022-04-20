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

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.Properties;
import de.natrox.pipeline.annotation.property.Context;
import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.gson.GsonProvider;
import de.natrox.pipeline.mongodb.MongoConfig;
import de.natrox.pipeline.mongodb.MongoProvider;
import de.natrox.pipeline.operator.filter.Filters;
import de.natrox.pipeline.redis.RedisConfig;
import de.natrox.pipeline.redis.RedisEndpoint;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class BenchmarkTest {

    private final static UUID ID = UUID.nameUUIDFromBytes("test".getBytes(StandardCharsets.UTF_8));
    private final static UUID ID_2 = UUID.nameUUIDFromBytes("test2".getBytes(StandardCharsets.UTF_8));

    public static void main(String[] args) throws Exception {
        var redisConfig = RedisConfig
            .builder()
            .endpoints(
                RedisEndpoint
                    .builder()
                    .host("127.0.0.1")
                    .port(6379)
                    .database(1)
                    .build()
            )
            .build();
        var redisProvider = redisConfig.createProvider();

        var mongoConfig = MongoConfig
            .builder()
            .host("127.0.0.1")
            .port(27017)
            .database("test")
            .build();
        MongoProvider mongoProvider = mongoConfig.createProvider();

        var registry = new PipelineRegistry();
        registry.register(Player.class);

        var pipeline = Pipeline
            .builder()
            .registry(registry)
            .dataUpdater(redisProvider)
            .globalCache(redisProvider)
            .globalStorage(mongoProvider)
            .jsonProvider(new GsonProvider())
            .build();

        pipeline.load(Player.class, UUID.randomUUID(), Pipeline.LoadingStrategy.LOAD_PIPELINE);

        var startInstant = Instant.now();

        var optionalPlayer = pipeline.load(Player.class, ID, Pipeline.LoadingStrategy.LOAD_PIPELINE, true);
        optionalPlayer.ifPresent(player -> {
            System.out.println(player.objectUUID());
            player.setName("Herbet");
            player.save();
        });

        var middleInstant = Instant.now();
        System.out.println(Duration.between(startInstant, middleInstant).toMillis());

        for (Player player : pipeline.find(Player.class).filter(Filters.field("name", "Herbet")).collect()) {

        }

        System.out.println(Duration.between(middleInstant, Instant.now()).toMillis());
    }

    @Properties(identifier = "PlayerBench", context = Context.GLOBAL)
    static class Player extends PipelineData {

        private String name;

        public Player(@NotNull Pipeline pipeline) {
            super(pipeline);
        }

        public Player setName(String name) {
            this.name = name;
            return this;
        }
    }

}
