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

package de.natrox.pipeline.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.Store;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
final class MongoProviderImpl implements MongoProvider {

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;

    MongoProviderImpl(@NotNull MongoClient mongoClient, @NotNull MongoDatabase mongoDatabase) {
        this.mongoClient = mongoClient;
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public void close() {
        this.mongoClient.close();
    }

    @Override
    public @NotNull Store createGlobalStorage(@NotNull Pipeline pipeline) {
        return new MongoStore(this.mongoDatabase);
    }
}
