package de.natrox.pipeline.mysql;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MySqlProvider implements GlobalStorageProvider {

    private static final String CONNECT_URL_FORMAT = "jdbc:mysql://%s:%d/%s?serverTimezone=UTC&useSSL=%b&trustServerCertificate=%b";

    private final MySqlConfig config;
    private @Nullable HikariDataSource hikariDataSource;

    protected MySqlProvider(@NotNull MySqlConfig config) {
        Preconditions.checkNotNull(config, "config");
        this.config = config;
    }

    @Override
    public boolean init() throws Exception {
        var config = new HikariConfig();
        var endpoint = this.config.randomEndpoint();

        config.setJdbcUrl(String.format(
            CONNECT_URL_FORMAT,
            endpoint.host(), endpoint.port(),
            endpoint.database(), endpoint.useSsl(), endpoint.useSsl()
        ));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setUsername(this.config.username());
        config.setPassword(this.config.password());

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

        config.setMinimumIdle(2);
        config.setMaximumPoolSize(100);
        config.setConnectionTimeout(10_000);
        config.setValidationTimeout(10_000);

        this.hikariDataSource = new HikariDataSource(config);
        return true;
    }

    @Override
    public void shutdown() {
        if (hikariDataSource != null) {
            hikariDataSource.close();
        }
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new MySqlStorage(pipeline, hikariDataSource);
    }
}
