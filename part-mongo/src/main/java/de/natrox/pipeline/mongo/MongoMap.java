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
import de.natrox.common.container.Pair;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.mapper.DocumentMapper;
import de.natrox.pipeline.part.StoreMap;
import de.natrox.pipeline.stream.PipeStream;
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
    private final DocumentMapper documentMapper;

    MongoMap(MongoCollection<Document> collection, DocumentMapper documentMapper) {
        this.collection = collection;
        this.documentMapper = documentMapper;
    }

    @Override
    public @Nullable DocumentData get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        Document document = this.collection
            .find(Filters.eq("key", uniqueId))
            .first();
        if (document == null)
            return null;

        byte[] bytes = document.get("data", Binary.class).getData();
        return this.documentMapper.read(bytes);
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull DocumentData documentData) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(documentData, "documentData");

        byte[] bytes = this.documentMapper.write(documentData);
        this.collection.updateOne(
            Filters.eq("key", uniqueId),
            Updates.combine(
                Updates.setOnInsert(new Document("key", uniqueId)),
                Updates.set("data", new Binary(bytes))
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
    public @NotNull PipeStream<UUID> keys() {
        List<UUID> keys = new ArrayList<>();
        try (var cursor = this.collection.find().iterator()) {
            while (cursor.hasNext()) {
                keys.add(cursor.next().get("key", UUID.class));
            }
        }
        return PipeStream.fromIterable(keys);
    }

    @Override
    public @NotNull PipeStream<DocumentData> values() {
        Collection<DocumentData> documents = new ArrayList<>();
        try (var cursor = this.collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                byte[] bytes = document.get("data", Binary.class).getData();
                documents.add(this.documentMapper.read(bytes));
            }
        }
        return PipeStream.fromIterable(documents);
    }

    @Override
    public @NotNull PipeStream<Pair<UUID, DocumentData>> entries() {
        Map<UUID, DocumentData> entries = new HashMap<>();
        try (var cursor = this.collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                UUID key = document.get("key", UUID.class);
                byte[] bytes = document.get("data", Binary.class).getData();
                DocumentData value = this.documentMapper.read(bytes);

                entries.put(key, value);
            }
        }
        return PipeStream.fromMap(entries);
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
