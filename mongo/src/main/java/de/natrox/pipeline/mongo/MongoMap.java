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
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import de.natrox.common.container.Pair;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.part.PartMap;
import de.natrox.pipeline.stream.PipeStream;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
final class MongoMap implements PartMap {

    //TODO: Add null checks

    private static final String KEY_NAME = "Key";
    private static final String VALUE_NAME = "Value";
    private static final UpdateOptions INSERT_OR_REPLACE_OPTIONS = new UpdateOptions().upsert(true);

    private final MongoCollection<Document> collection;
    private final JsonConverter jsonConverter;

    MongoMap(MongoCollection<Document> collection, JsonConverter jsonConverter) {
        this.collection = collection;
        this.jsonConverter = jsonConverter;
    }

    @Override
    public @Nullable DocumentData get(@NotNull UUID uniqueId) {
        Document document = collection
            .find(Filters.eq(KEY_NAME, uniqueId))
            .first();
        if (document == null)
            return null;

        Document valueDocument = document.get(VALUE_NAME, Document.class);
        return this.jsonConverter.fromJson(valueDocument.toJson(), DocumentData.class);
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull DocumentData documentData) {
        this.collection.updateOne(
            Filters.eq(KEY_NAME, uniqueId),
            Updates.combine(
                Updates.setOnInsert(new Document(KEY_NAME, uniqueId)),
                Updates.set(VALUE_NAME, Document.parse(this.jsonConverter.toJson(documentData)))
            ),
            INSERT_OR_REPLACE_OPTIONS);
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        Document document = this.collection
            .find(Filters.eq(KEY_NAME, uniqueId))
            .first();

        return document != null;
    }

    @Override
    public @NotNull PipeStream<UUID> keys() {
        List<UUID> keys = new ArrayList<>();
        try (var cursor = this.collection.find().iterator()) {
            while (cursor.hasNext()) {
                keys.add(cursor.next().get(KEY_NAME, UUID.class));
            }
        }
        return PipeStream.fromIterable(keys);
    }

    @Override
    public @NotNull PipeStream<DocumentData> values() {
        Collection<DocumentData> documents = new ArrayList<>();
        try (var cursor = this.collection.find().iterator()) {
            while (cursor.hasNext()) {
                documents.add(this.jsonConverter.fromJson(cursor.next().get(VALUE_NAME, Document.class).toJson(), DocumentData.class));
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
                UUID key = document.get(KEY_NAME, UUID.class);
                DocumentData value = this.jsonConverter.fromJson(document.get(VALUE_NAME, Document.class).toJson(), DocumentData.class);

                entries.put(key, value);
            }
        }
        return PipeStream.fromMap(entries);
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        this.collection.deleteOne(Filters.eq(KEY_NAME, uniqueId));
    }

    @Override
    public void clear() {

    }

    @Override
    public long size() {
        return this.collection.countDocuments();
    }
}
