package de.natrox.pipeline.mysql;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import org.jetbrains.annotations.NotNull;

public final class MySqlProvider implements GlobalStorageProvider {

    private static final String CONNECT_URL_FORMAT = "jdbc:mysql://%s:%d/%s?serverTimezone=UTC&useSSL=%b&trustServerCertificate=%b";
    private final HikariDataSource hikariDataSource;

    protected MySqlProvider(@NotNull MySqlConfig config) throws Exception {
        Preconditions.checkNotNull(config, "config");

        var hikariConfig = new HikariConfig();
        var endpoint = config.randomEndpoint();

        hikariConfig.setJdbcUrl(String.format(
            CONNECT_URL_FORMAT,
            endpoint.host(), endpoint.port(),
            endpoint.database(), endpoint.useSsl(), endpoint.useSsl()
        ));
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setUsername(config.username());
        hikariConfig.setPassword(config.password());

        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");

        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(100);
        hikariConfig.setConnectionTimeout(10_000);
        hikariConfig.setValidationTimeout(10_000);

        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void shutdown() {
        hikariDataSource.close();
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new MySqlStorage(pipeline, hikariDataSource);
    }
}
