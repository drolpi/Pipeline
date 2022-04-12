package de.natrox.pipeline.mongodb;

import com.google.common.base.Preconditions;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MongoProvider implements GlobalStorageProvider {

    private final MongoConfig config;
    private @Nullable MongoClient mongoClient;
    private @Nullable MongoDatabase mongoDatabase;

    protected MongoProvider(@NotNull MongoConfig config) {
        Preconditions.checkNotNull(config, "config");
        this.config = config;
    }

    @Override
    public boolean init() throws Exception {
        this.mongoClient = MongoClients.create(this.config.buildConnectionUri());
        this.mongoDatabase = this.mongoClient.getDatabase(this.config.database());

        return true;
    }

    @Override
    public void shutdown() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new MongoStorage(pipeline, mongoDatabase);
    }
}
