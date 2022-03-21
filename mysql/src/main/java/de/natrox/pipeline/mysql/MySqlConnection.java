package de.natrox.pipeline.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.config.connection.Connection;
import de.natrox.pipeline.config.connection.GlobalStorageConnection;
import de.natrox.pipeline.part.storage.GlobalStorage;

public class MySqlConnection implements GlobalStorageConnection, Connection {

    private static final String CONNECT_URL_FORMAT = "jdbc:mysql://%s:%d/%s?serverTimezone=UTC&useSSL=%b&trustServerCertificate=%b";

    private final String host;
    private final int port;
    private final boolean useSsl;
    private final String database;
    private final String user;
    private final String password;

    private HikariDataSource hikariDataSource;
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
        var config = new HikariConfig();

        config.setJdbcUrl(String.format(
            CONNECT_URL_FORMAT,
            host, port,
            database, useSsl, useSsl
        ));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setUsername(user);
        config.setPassword(password);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        config.setMinimumIdle(2);
        config.setMaximumPoolSize(100);
        config.setConnectionTimeout(10_000);
        config.setValidationTimeout(10_000);

        this.hikariDataSource = new HikariDataSource(config);
        connected = true;
    }

    @Override
    public void shutdown() {
        hikariDataSource.close();
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
