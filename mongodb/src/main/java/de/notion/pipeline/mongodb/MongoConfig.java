package de.notion.pipeline.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import de.notion.pipeline.Pipeline;
import de.notion.pipeline.config.PartConfig;
import de.notion.pipeline.config.part.GlobalStorageConfig;
import de.notion.pipeline.mongodb.storage.MongoStorage;
import de.notion.pipeline.part.storage.GlobalStorage;

public class MongoConfig implements GlobalStorageConfig, PartConfig {

    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private boolean connected;

    public MongoConfig(String host, int port, String database) {
        this(host, port, database, "", "");
    }

    public MongoConfig(String host, int port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        this.connected = false;
    }

    @Override
    public void load() {
        if (isLoaded())
            return;
        if (user.isEmpty() && password.isEmpty())
            this.mongoClient = new MongoClient(host, port);
        else
            this.mongoClient = new MongoClient(
                    new ServerAddress(host, port),
                    MongoCredential.createCredential(user, database, password.toCharArray()),
                    MongoClientOptions.builder().build()
            );

        this.mongoDatabase = mongoClient.getDatabase(database);
        connected = true;
    }

    @Override
    public void shutdown() {
        mongoClient.close();
        connected = false;
    }

    @Override
    public boolean isLoaded() {
        return connected;
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new MongoStorage(pipeline, mongoDatabase);
    }
}
