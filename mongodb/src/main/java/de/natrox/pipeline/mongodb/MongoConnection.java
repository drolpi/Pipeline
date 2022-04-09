package de.natrox.pipeline.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.config.connection.Connection;
import de.natrox.pipeline.config.connection.GlobalStorageConnection;
import de.natrox.pipeline.part.storage.GlobalStorage;
import org.jetbrains.annotations.Nullable;

public class MongoConnection implements GlobalStorageConnection, Connection {

    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;

    private @Nullable MongoClient mongoClient;
    private @Nullable MongoDatabase mongoDatabase;
    private boolean connected;

    public MongoConnection(String host, int port, String database) {
        this(host, port, database, "", "");
    }

    public MongoConnection(String host, int port, String database, String user, String password) {
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
        if (mongoClient != null) {
            mongoClient.close();
        }
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
