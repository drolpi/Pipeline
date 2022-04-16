package de.natrox.pipeline.mongodb;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.part.storage.GlobalStorage;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class MongoStorage implements GlobalStorage {

    private final static Logger LOGGER = LogManager.logger(MongoStorage.class);

    private final Gson gson;
    private final MongoDatabase mongoDatabase;

    protected MongoStorage(Pipeline pipeline, MongoDatabase mongoDatabase) {
        this.gson = pipeline.gson();
        this.mongoDatabase = mongoDatabase;

        LOGGER.debug("Mongo storage initialized");
    }

    @Override
    public synchronized JsonObject loadData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        var filter = new Document("objectUUID", objectUUID.toString());
        var mongoDBData = mongoStorage(dataClass).find(filter).first();

        if (mongoDBData == null)
            mongoDBData = filter;

        mongoDBData.remove("_id");

        return gson.toJsonTree(mongoDBData).getAsJsonObject();
    }

    @Override
    public synchronized boolean dataExist(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        return mongoStorage(dataClass).find(new Document("objectUUID", objectUUID.toString())).first() != null;
    }

    @Override
    public synchronized void saveData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonObject data) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        Preconditions.checkNotNull(data, "data");

        var filter = new Document("objectUUID", objectUUID.toString());
        var collection = mongoStorage(dataClass);

        if (collection.find(filter).first() == null) {
            var newData = Document.parse(gson.toJson(data));
            collection.insertOne(newData);
        } else {
            var newData = Document.parse(gson.toJson(data));
            var updateFunc = new Document("$set", newData);
            collection.updateOne(filter, updateFunc);
        }
    }

    @Override
    public synchronized boolean removeData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        var filter = new Document("objectUUID", objectUUID.toString());
        var collection = mongoStorage(dataClass);

        return collection.deleteOne(filter).getDeletedCount() >= 1;
    }

    @Override
    public synchronized @NotNull Collection<UUID> savedUUIDs(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        return data(dataClass).keySet();
    }

    @Override
    public @NotNull Map<UUID, JsonObject> data(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        var collection = mongoStorage(dataClass);
        var data = new HashMap<UUID, JsonObject>();

        try (var cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                var document = cursor.next();
                if (!document.containsKey("objectUUID"))
                    continue;
                data.put(UUID.fromString((String) document.get("objectUUID")), gson.toJsonTree(document).getAsJsonObject());
            }
        }
        return data;
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
