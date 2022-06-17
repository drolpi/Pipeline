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
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.provider.GlobalStorageProvider;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;

public sealed interface MongoProvider extends GlobalStorageProvider permits MongoProviderImpl {

    static @NotNull MongoConfig createConfig() {
        return new MongoConfig();
    }

    static @NotNull MongoProvider of(@NotNull MongoClient mongoClient, @NotNull MongoDatabase mongoDatabase) {
        Check.notNull(mongoClient, "mongoClient");
        Check.notNull(mongoDatabase, "mongoDatabase");
        return new MongoProviderImpl(mongoClient, mongoDatabase);
    }

    static @NotNull MongoProvider of(@NotNull MongoClient mongoClient, @NotNull String databaseName) {
        Check.notNull(mongoClient, "mongoClient");
        Check.notNull(databaseName, "databaseName");
        return MongoProvider.of(mongoClient, mongoClient.getDatabase(databaseName));
    }

    static @NotNull MongoProvider of(@NotNull MongoConfig config) throws UnsupportedEncodingException {
        Check.notNull(config, "config");
        return MongoProvider.of(MongoClients.create(config.buildConnectionUri()), config.database);
    }
}
