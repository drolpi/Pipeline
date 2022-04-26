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
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import de.natrox.common.container.Pair;
import de.natrox.pipeline.document.PipeDocument;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.part.map.PartMap;
import de.natrox.pipeline.stream.PipeStream;
import de.natrox.pipeline.util.StreamUtil;
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

    private static final String KEY_NAME = "Key";
    private static final String VALUE_NAME = "Value";
    private static final IndexOptions UNIQUE_KEY_OPTIONS = new IndexOptions().unique(true);
    private static final UpdateOptions INSERT_OR_REPLACE_OPTIONS = new UpdateOptions().upsert(true);

    private final MongoCollection<Document> collection;
    private final JsonConverter jsonConverter;

    public MongoMap(MongoCollection<Document> collection, JsonConverter jsonConverter) {
        this.collection = collection;
        this.jsonConverter = jsonConverter;
    }

    @Override
    public @Nullable PipeDocument get(@NotNull UUID uniqueId) {
        Document document = collection
            .find(Filters.eq(KEY_NAME, uniqueId))
            .first();
        if (document == null)
            return null;

        Document valueDocument = document.get(VALUE_NAME, Document.class);
        return jsonConverter.fromJson(valueDocument.toJson(), PipeDocument.class);
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull PipeDocument document) {
        collection.updateOne(
            Filters.eq(KEY_NAME, uniqueId),
            Updates.combine(
                Updates.setOnInsert(new Document(KEY_NAME, uniqueId)),
                Updates.set(VALUE_NAME, Document.parse(jsonConverter.toJson(document)))
            ),
            INSERT_OR_REPLACE_OPTIONS);
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        Document document = collection
            .find(Filters.eq(KEY_NAME, uniqueId))
            .first();

        return document != null;
    }

    @Override
    public @NotNull PipeStream<UUID> keys() {
        List<UUID> keys = new ArrayList<>();
        try (var cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                keys.add(cursor.next().get(KEY_NAME, UUID.class));
            }
        }
        return PipeStream.fromIterable(keys);
    }

    @Override
    public @NotNull PipeStream<PipeDocument> values() {
        Collection<PipeDocument> documents = new ArrayList<>();
        try (var cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                documents.add(jsonConverter.fromJson(cursor.next().get(VALUE_NAME, Document.class).toJson(), PipeDocument.class));
            }
        }
        return PipeStream.fromIterable(documents);
    }

    @Override
    public @NotNull PipeStream<Pair<UUID, PipeDocument>> entries() {
        Map<UUID, PipeDocument> entries = new HashMap<>();
        try (var cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                UUID key = document.get(KEY_NAME, UUID.class);
                PipeDocument value = jsonConverter.fromJson(document.get(VALUE_NAME, Document.class).toJson(), PipeDocument.class);

                entries.put(key, value);
            }
        }
        return StreamUtil.streamForMap(entries);
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        collection.deleteOne(Filters.eq(KEY_NAME, uniqueId));
    }

    @Override
    public void clear() {

    }

    @Override
    public long size() {
        return collection.countDocuments();
    }
}
