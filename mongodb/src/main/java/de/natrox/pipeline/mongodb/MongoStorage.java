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

package de.natrox.pipeline.mongodb;

import com.google.common.base.Preconditions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.part.storage.GlobalStorage;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

final class MongoStorage implements GlobalStorage {

    protected static final String KEY_NAME = "Key";
    protected static final String VALUE_NAME = "Value";
    protected static final IndexOptions UNIQUE_KEY_OPTIONS = new IndexOptions().unique(true);
    protected static final UpdateOptions INSERT_OR_REPLACE_OPTIONS = new UpdateOptions().upsert(true);

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoStorage.class);

    private final JsonDocument.Factory documentFactory;
    private final MongoDatabase mongoDatabase;

    protected MongoStorage(Pipeline pipeline, MongoDatabase mongoDatabase) {
        this.documentFactory = pipeline.documentFactory();
        this.mongoDatabase = mongoDatabase;

        LOGGER.debug("Mongo storage initialized");
    }

    @Override
    public synchronized JsonDocument get(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        var collection = mongoStorage(dataClass);
        var document = collection
            .find(Filters.eq(KEY_NAME, objectUUID))
            .first();

        return document == null ? null : documentFactory.fromJsonString(document.get(VALUE_NAME, Document.class).toJson());
    }

    @Override
    public synchronized boolean exists(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        var collection = mongoStorage(dataClass);
        var document = collection
            .find(Filters.eq(KEY_NAME, objectUUID))
            .first();

        return document != null;
    }

    @Override
    public synchronized void save(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonDocument data) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        Preconditions.checkNotNull(data, "data");

        var collection = mongoStorage(dataClass);
        collection.updateOne(
            Filters.eq(KEY_NAME, objectUUID),
            Updates.combine(
                Updates.setOnInsert(new Document(KEY_NAME, objectUUID)),
                Updates.set(VALUE_NAME, Document.parse(data.toString()))
            ),
            INSERT_OR_REPLACE_OPTIONS);
    }

    @Override
    public synchronized boolean remove(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        var collection = mongoStorage(dataClass);
        return collection.deleteOne(Filters.eq(KEY_NAME, objectUUID)).getDeletedCount() > 0;
    }

    @Override
    public synchronized @NotNull Collection<UUID> keys(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");

        var collection = mongoStorage(dataClass);
        Collection<UUID> keys = new ArrayList<>();
        try (var cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                keys.add(cursor.next().get(KEY_NAME, UUID.class));
            }
        }
        return keys;
    }

    @Override
    public @NotNull Collection<JsonDocument> documents(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");

        var collection = mongoStorage(dataClass);
        Collection<JsonDocument> documents = new ArrayList<>();
        try (var cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                documents.add(documentFactory.fromJsonString(cursor.next().get(VALUE_NAME, Document.class).toJson()));
            }
        }
        return documents;
    }

    @Override
    public @NotNull Map<UUID, JsonDocument> entries(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        return this.filter(dataClass, (key, value) -> true);
    }

    @Override
    public @NotNull Map<UUID, JsonDocument> filter(@NotNull Class<? extends PipelineData> dataClass, @NotNull BiPredicate<UUID, JsonDocument> predicate) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(predicate, "predicate");

        var collection = mongoStorage(dataClass);
        Map<UUID, JsonDocument> entries = new HashMap<>();
        try (var cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                var document = cursor.next();
                var key = document.get(KEY_NAME, UUID.class);
                var value = documentFactory.fromJsonString(document.get(VALUE_NAME, Document.class).toJson());

                if (predicate.test(key, value)) {
                    entries.put(key, value);
                }
            }
        }
        return entries;
    }

    @Override
    public void iterate(@NotNull Class<? extends PipelineData> dataClass, @NotNull BiConsumer<UUID, JsonDocument> consumer) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(consumer, "consumer");
        this.entries(dataClass).forEach(consumer);
    }

    private synchronized MongoCollection<Document> mongoStorage(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        return collection(AnnotationResolver.storageIdentifier(dataClass));
    }

    private synchronized MongoCollection<Document> collection(@NotNull String name) {
        Preconditions.checkNotNull(name, "name");
        try {
            return mongoDatabase.getCollection(name);
        }
        // Collection does not exist
        catch (IllegalArgumentException e) {
            mongoDatabase.createCollection(name);
            return mongoDatabase.getCollection(name);
        }
    }
}
