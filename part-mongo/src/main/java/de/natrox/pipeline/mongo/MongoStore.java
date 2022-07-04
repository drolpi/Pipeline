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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.store.AbstractStore;
import de.natrox.pipeline.part.store.StoreMap;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

final class MongoStore extends AbstractStore {

    private final MongoDatabase mongoDatabase;

    MongoStore(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    protected StoreMap createMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        return new MongoMap(this.collection(mapName));
    }

    @Override
    public @NotNull Set<String> maps() {
        Set<String> names = new HashSet<>();
        for (String name : this.mongoDatabase.listCollectionNames()) {
            names.add(name);
        }
        return names;
    }

    @Override
    public boolean hasMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        try {
            this.mongoDatabase.getCollection(mapName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void removeMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        this.collection(mapName).drop();
        this.storeMapRegistry.remove(mapName);
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
