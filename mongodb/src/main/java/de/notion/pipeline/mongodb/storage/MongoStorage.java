package de.notion.pipeline.mongodb.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.filter.Filter;
import de.notion.pipeline.part.storage.GlobalStorage;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MongoStorage implements GlobalStorage {

    protected static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private final MongoDatabase mongoDatabase;

    public MongoStorage(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
        System.out.println("Mongo Global Storage started"); //DEBUG
    }

    @Override
    public synchronized String loadData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        var filter = new Document("objectUUID", objectUUID.toString());

        var mongoDBData = mongoStorage(dataClass).find(filter).first();

        if (mongoDBData == null)
            mongoDBData = filter;

        mongoDBData.remove("_id");

        return GSON.toJson(mongoDBData);
    }

    @Override
    public synchronized boolean dataExist(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        var document = mongoStorage(dataClass).find(new Document("objectUUID", objectUUID.toString())).first();
        return document != null;
    }

    @Override
    public synchronized void saveData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull String dataToSave) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        Objects.requireNonNull(dataToSave, "dataToSave can't be null!");
        var filter = new Document("objectUUID", objectUUID.toString());

        var collection = mongoStorage(dataClass);

        if (collection.find(filter).first() == null) {
            var newData = Document.parse(dataToSave);
            collection.insertOne(newData);
        } else {
            var newData = Document.parse(dataToSave);
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
    public synchronized Set<UUID> savedUUIDs(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        var collection = mongoStorage(dataClass);
        Set<UUID> uuids = new HashSet<>();
        try (var cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                var document = cursor.next();
                if (!document.containsKey("objectUUID"))
                    continue;
                uuids.add(UUID.fromString((String) document.get("objectUUID")));
            }
        }
        return uuids;
    }

    @Override
    public List<UUID> filter(@NotNull Class<? extends PipelineData> type, @NotNull Filter filter) {
        var collection = mongoStorage(type);
        List<UUID> uuids = new ArrayList<>();
        try (var cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                var document = cursor.next();

                if (filter.check(document)) {
                    uuids.add(UUID.fromString((String) document.get("objectUUID")));
                }
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
