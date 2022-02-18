package de.notion.pipeline.sql.h2;

import de.notion.pipeline.config.PartConfig;
import de.notion.pipeline.config.part.GlobalStorageConfig;
import de.notion.pipeline.part.storage.GlobalStorage;
import org.h2.Driver;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Config implements GlobalStorageConfig, PartConfig {

    static {
        Driver.load();
    }

    private final String h2dbFile;

    private Connection connection;
    private boolean connected;

    public H2Config(Path h2dbFile) {
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
    public GlobalStorage constructGlobalStorage() {
        return new H2Storage(connection);
    }
}
