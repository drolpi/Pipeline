package de.natrox.pipeline.h2;

import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.config.connection.Connection;
import de.natrox.pipeline.config.connection.GlobalStorageConnection;
import org.h2.Driver;

import java.io.File;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Connection implements GlobalStorageConnection, Connection {

    static {
        Driver.load();
    }

    private final String h2dbFile;

    private java.sql.Connection connection;
    private boolean connected;

    public H2Connection(Path h2dbFile) {
        this.h2dbFile = h2dbFile.toString();
    }

    @Override
    public void load() {
        var file = new File(h2dbFile);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            this.connection = DriverManager.getConnection("jdbc:h2:" + file.getAbsolutePath());
            connected = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isLoaded() {
        return connected;
    }

    @Override
    public void shutdown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        connected = false;
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new H2Storage(pipeline, connection);
    }
}
