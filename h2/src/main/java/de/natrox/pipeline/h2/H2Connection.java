package de.natrox.pipeline.h2;

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.config.connection.Connection;
import de.natrox.pipeline.config.connection.GlobalStorageConnection;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.sql.HikariUtil;
import org.h2.Driver;
import org.h2.jdbcx.JdbcDataSource;

import java.io.File;
import java.nio.file.Path;

public class H2Connection implements GlobalStorageConnection, Connection {

    static {
        Driver.load();
    }

    private final String h2dbFile;

    private HikariDataSource hikariDataSource;
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

        if (isLoaded()) return;
        this.hikariDataSource = HikariUtil.createDataSource(config -> {
            var dataSource = new JdbcDataSource();
            dataSource.setUrl("jdbc:h2:file:" + file.getAbsolutePath());
            config.setDataSource(dataSource);
        });
        connected = true;
    }

    @Override
    public boolean isLoaded() {
        return connected;
    }

    @Override
    public void shutdown() {
        hikariDataSource.close();
        connected = false;
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new H2Storage(pipeline, hikariDataSource);
    }
}
