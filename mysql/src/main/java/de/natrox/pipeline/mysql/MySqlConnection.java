package de.natrox.pipeline.mysql;

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.config.connection.Connection;
import de.natrox.pipeline.config.connection.GlobalStorageConnection;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.sql.HikariUtil;
import org.jetbrains.annotations.Nullable;

public class MySqlConnection implements GlobalStorageConnection, Connection {

    private static final String CONNECT_URL_FORMAT = "jdbc:mysql://%s:%d/%s?serverTimezone=UTC&useSSL=%b&trustServerCertificate=%b";

    private final String host;
    private final int port;
    private final boolean useSsl;
    private final String database;
    private final String user;
    private final String password;

    private @Nullable HikariDataSource hikariDataSource;
    private boolean connected;

    public MySqlConnection(String host, int port, boolean useSsl, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.useSsl = useSsl;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    @Override
    public void load() {
        if (isLoaded()) return;
        this.hikariDataSource = HikariUtil.createDataSource(config -> {
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.addDataSourceProperty("maintainTimeStats", "false");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");

            config.setJdbcUrl(String.format(
                CONNECT_URL_FORMAT,
                host, port,
                database, useSsl, useSsl
            ));
            config.setUsername(user);
            config.setPassword(password);
        });
        connected = true;
    }

    @Override
    public void shutdown() {
        if (hikariDataSource != null) {
            hikariDataSource.close();
        }
        connected = false;
    }

    @Override
    public boolean isLoaded() {
        return connected;
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new MySqlStorage(pipeline, hikariDataSource);
    }
}
