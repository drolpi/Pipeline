package de.notion.pipeline.mongodb.storage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.notion.pipeline.Pipeline;
import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.operator.FindOptions;
import de.notion.pipeline.part.storage.GlobalStorage;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MongoStorage implements GlobalStorage {

    private final Gson gson;
    private final MongoDatabase mongoDatabase;

    public MongoStorage(Pipeline pipeline, MongoDatabase mongoDatabase) {
        this.gson = pipeline.gson();
        this.mongoDatabase = mongoDatabase;
        System.out.println("Mongo storage started"); //DEBUG
    }

    @Override
    public synchronized JsonObject loadData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");

        var filter = new Document("objectUUID", objectUUID.toString());
        var mongoDBData = mongoStorage(dataClass).find(filter).first();

        if (mongoDBData == null)
            mongoDBData = filter;

        mongoDBData.remove("_id");

        return gson.toJsonTree(mongoDBData).getAsJsonObject();
    }

    @Override
    public synchronized boolean dataExist(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");

        return mongoStorage(dataClass).find(new Document("objectUUID", objectUUID.toString())).first() != null;
    }

    @Override
    public synchronized void saveData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonObject dataToSave) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        Objects.requireNonNull(dataToSave, "dataToSave can't be null!");

        var filter = new Document("objectUUID", objectUUID.toString());
        var collection = mongoStorage(dataClass);

        if (collection.find(filter).first() == null) {
            var newData = Document.parse(gson.toJson(dataToSave));
            collection.insertOne(newData);
        } else {
            var newData = Document.parse(gson.toJson(dataToSave));
            var updateFunc = new Document("$set", newData);
            collection.updateOne(filter, updateFunc);
        }
    }

    @Override
    public synchronized boolean removeData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");

        var filter = new Document("objectUUID", objectUUID.toString());
        var collection = mongoStorage(dataClass);

        return collection.deleteOne(filter).getDeletedCount() >= 1;
    }

    @Override
    public synchronized List<UUID> savedUUIDs(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        return findUUIDs(dataClass, new FindOptions());
    }

    @Override
    @NotNull
    public List<UUID> findUUIDs(@NotNull Class<? extends PipelineData> dataClass, @NotNull FindOptions findOptions) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");

        var collection = mongoStorage(dataClass);
        var uuids = new ArrayList<UUID>();

        var filter = findOptions.filter();
        var skip = findOptions.skip();
        var limit = findOptions.limit() + skip;

        try (var cursor = collection.find().iterator()) {
            for (int i = 0; cursor.hasNext(); i++) {
                var document = cursor.next();
                if (skip > i)
                    continue;
                if (i > limit)
                    break;

                if (filter != null && !filter.check(gson.toJsonTree(document).getAsJsonObject()))
                    continue;
                if (!document.containsKey("objectUUID"))
                    continue;
                uuids.add(UUID.fromString((String) document.get("objectUUID")));
            }
        }
        return uuids;
    }

    private synchronized MongoCollection<Document> mongoStorage(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        return collection(AnnotationResolver.storageIdentifier(dataClass));
    }

    private synchronized MongoCollection<Document> collection(@NotNull String name) {
        Objects.requireNonNull(name, "name can't be null!");
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
