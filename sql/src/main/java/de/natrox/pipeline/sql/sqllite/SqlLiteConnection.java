package de.natrox.pipeline.sql.sqllite;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.config.connection.Connection;
import de.natrox.pipeline.config.connection.GlobalStorageConnection;
import de.natrox.pipeline.part.storage.GlobalStorage;

import java.io.File;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlLiteConnection implements GlobalStorageConnection, Connection {

    private final String sqlLiteFile;
    private java.sql.Connection connection;
    private boolean connected;

    public SqlLiteConnection(Path sqlLiteFile) {
        this.sqlLiteFile = sqlLiteFile.toString();
    }

    @Override
    public void load() {
        var file = new File(sqlLiteFile);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            connected = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new SqlLiteStorage(pipeline, connection);
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
}
