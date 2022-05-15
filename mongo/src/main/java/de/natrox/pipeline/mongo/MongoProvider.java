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

package de.natrox.pipeline.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.provider.GlobalStorageProvider;
import de.natrox.pipeline.part.Store;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;

public final class MongoProvider implements GlobalStorageProvider {

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;

    MongoProvider(@NotNull MongoConfig config) throws UnsupportedEncodingException {
        Check.notNull(config, "config");

        this.mongoClient = MongoClients.create(config.buildConnectionUri());
        this.mongoDatabase = this.mongoClient.getDatabase(config.database());
    }

    @Override
    public void close() {

    }

    @Override
    public @NotNull Store createGlobalStorage(@NotNull Pipeline pipeline) {
        return new MongoStorage(pipeline, this.mongoDatabase);
    }
}
