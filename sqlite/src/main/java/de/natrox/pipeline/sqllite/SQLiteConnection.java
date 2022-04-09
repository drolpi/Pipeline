package de.natrox.pipeline.sqllite;

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.config.connection.Connection;
import de.natrox.pipeline.config.connection.GlobalStorageConnection;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.sql.HikariUtil;
import org.jetbrains.annotations.Nullable;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.nio.file.Path;

public class SQLiteConnection implements GlobalStorageConnection, Connection {

    private final String sqlLiteFile;

    private @Nullable HikariDataSource hikariDataSource;
    private boolean connected;

    public SQLiteConnection(Path sqlLiteFile) {
        this.sqlLiteFile = sqlLiteFile.toString();
    }

    @Override
    public void load() {
        var file = new File(sqlLiteFile);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (isLoaded()) return;
        this.hikariDataSource = HikariUtil.createDataSource(config -> {
            var dataSource = new SQLiteDataSource();
            dataSource.setUrl("jdbc:sqlite:" + file.getAbsolutePath());
            config.setDataSource(dataSource);
        });
        connected = true;
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new SQLiteStorage(pipeline, hikariDataSource);
    }

    @Override
    public boolean isLoaded() {
        return connected;
    }

    @Override
    public void shutdown() {
        if (hikariDataSource != null) {
            hikariDataSource.close();
        }
        connected = false;
    }
}
