package de.natrox.pipeline.mongodb;

import com.google.common.base.Preconditions;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import org.jetbrains.annotations.NotNull;

public final class MongoProvider implements GlobalStorageProvider {

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;

    protected MongoProvider(@NotNull MongoConfig config) throws Exception {
        Preconditions.checkNotNull(config, "config");

        this.mongoClient = MongoClients.create(config.buildConnectionUri());
        this.mongoDatabase = this.mongoClient.getDatabase(config.database());
    }

    @Override
    public void shutdown() {
        this.mongoClient.close();
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new MongoStorage(pipeline, mongoDatabase);
    }
}
