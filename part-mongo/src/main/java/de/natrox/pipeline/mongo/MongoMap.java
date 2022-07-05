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
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.store.StoreMap;
import org.bson.Document;
import org.bson.types.Binary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
final class MongoMap implements StoreMap {

    private static final UpdateOptions INSERT_OR_REPLACE_OPTIONS = new UpdateOptions().upsert(true);

    private final MongoCollection<Document> collection;

    MongoMap(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    @Override
    public byte @Nullable [] get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        Document document = this.collection
            .find(Filters.eq("key", uniqueId))
            .first();
        if (document == null)
            return null;

        return document.get("data", Binary.class).getData();
    }

    @Override
    public void put(@NotNull UUID uniqueId, byte @NotNull [] data) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(data, "data");

        this.collection.updateOne(
            Filters.eq("key", uniqueId),
            Updates.combine(
                Updates.setOnInsert(new Document("key", uniqueId)),
                Updates.set("data", new Binary(data))
            ),
            INSERT_OR_REPLACE_OPTIONS
        );
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        Document document = this.collection
            .find(Filters.eq("key", uniqueId))
            .first();

        return document != null;
    }

    @Override
    public @NotNull Collection<UUID> keys() {
        List<UUID> keys = new ArrayList<>();
        try (var cursor = this.collection.find().iterator()) {
            while (cursor.hasNext()) {
                keys.add(cursor.next().get("key", UUID.class));
            }
        }
        return keys;
    }

    @Override
    public @NotNull Collection<byte[]> values() {
        Collection<byte[]> documents = new ArrayList<>();
        try (var cursor = this.collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                byte[] bytes = document.get("data", Binary.class).getData();
                documents.add(bytes);
            }
        }
        return documents;
    }

    @Override
    public @NotNull Map<UUID, byte[]> entries() {
        Map<UUID, byte[]> entries = new HashMap<>();
        try (var cursor = this.collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                UUID key = document.get("key", UUID.class);
                byte[] bytes = document.get("data", Binary.class).getData();

                entries.put(key, bytes);
            }
        }
        return entries;
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        this.collection.deleteOne(Filters.eq("key", uniqueId));
    }

    @Override
    public void clear() {
        this.collection.deleteMany(new Document());
    }

    @Override
    public long size() {
        return this.collection.countDocuments();
    }
}
