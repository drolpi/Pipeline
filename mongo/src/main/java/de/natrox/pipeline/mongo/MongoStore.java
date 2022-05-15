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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.part.AbstractStore;
import de.natrox.pipeline.part.StoreMap;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class MongoStore extends AbstractStore {

    private final JsonConverter jsonConverter;
    private final MongoDatabase mongoDatabase;
    private final Map<String, MongoMap> mongoMapRegistry;

    MongoStore(Pipeline pipeline, MongoDatabase mongoDatabase) {
        this.jsonConverter = pipeline.jsonConverter();
        this.mongoDatabase = mongoDatabase;
        this.mongoMapRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public @NotNull StoreMap openMap(@NotNull String mapName) {
        if (this.mongoMapRegistry.containsKey(mapName)) {
            return this.mongoMapRegistry.get(mapName);
        }
        MongoMap mongoMap = new MongoMap(collection(mapName), this.jsonConverter);
        this.mongoMapRegistry.put(mapName, mongoMap);

        return mongoMap;
    }

    @Override
    public boolean hasMap(@NotNull String mapName) {
        try {
            this.mongoDatabase.getCollection(mapName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void closeMap(@NotNull String mapName) {
        this.mongoMapRegistry.remove(mapName);
    }

    @Override
    public void removeMap(@NotNull String mapName) {
        this.collection(mapName).drop();
        this.mongoMapRegistry.remove(mapName);
    }

    @Override
    public boolean isClosed() {
        //TODO:
        return false;
    }

    @Override
    public void close() {
        //TODO:
    }

    private MongoCollection<Document> collection(String name) {
        try {
            return this.mongoDatabase.getCollection(name);
        } catch (IllegalArgumentException e) {
            this.mongoDatabase.createCollection(name);
            return this.mongoDatabase.getCollection(name);
        }
    }

}
