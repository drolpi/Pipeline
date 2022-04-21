/*
 * Copyright 2020-2022 NatroxMC team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.natrox.pipeline.mysql;

import de.natrox.common.validate.Check;
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
        Check.notNull(config, "config");

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
