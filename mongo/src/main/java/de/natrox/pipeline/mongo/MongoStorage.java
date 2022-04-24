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
import de.natrox.pipeline.part.map.PartMap;
import de.natrox.pipeline.part.storage.GlobalStorage;
import org.bson.Document;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class MongoStorage implements GlobalStorage {

    private final JsonConverter jsonConverter;
    private final MongoDatabase mongoDatabase;
    private final Map<String, MongoMap> mongoMapRegistry;

    MongoStorage(Pipeline pipeline, MongoDatabase mongoDatabase) {
        this.jsonConverter = pipeline.jsonConverter();
        this.mongoDatabase = mongoDatabase;
        this.mongoMapRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public PartMap openMap(String mapName) {
        if (mongoMapRegistry.containsKey(mapName)) {
            return mongoMapRegistry.get(mapName);
        }
        MongoMap mongoMap = new MongoMap(collection(mapName), jsonConverter);
        mongoMapRegistry.put(mapName, mongoMap);

        return mongoMap;
    }

    private MongoCollection<Document> collection(String name) {
        try {
            return mongoDatabase.getCollection(name);
        } catch (IllegalArgumentException e) {
            mongoDatabase.createCollection(name);
            return mongoDatabase.getCollection(name);
        }
    }

}
