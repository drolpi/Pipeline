/*
 * Copyright 2020-2022 NatroxMC
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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.provider.GlobalStorageProvider;
import org.jetbrains.annotations.NotNull;

public sealed interface MySqlProvider extends GlobalStorageProvider permits MySqlProviderImpl {

    static @NotNull MySqlConfig createConfig() {
        return new MySqlConfig();
    }

    static @NotNull MySqlProvider of(@NotNull HikariDataSource hikariDataSource, @NotNull String databaseName) {
        Check.notNull(hikariDataSource, "hikariDataSource");
        Check.notNull(databaseName, "databaseName");
        return new MySqlProviderImpl(hikariDataSource, databaseName);
    }

    static @NotNull MySqlProvider of(@NotNull MySqlConfig config) {
        Check.notNull(config, "config");
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(String.format(
            "jdbc:mysql://%s:%d/%s?serverTimezone=UTC&useSSL=%b&trustServerCertificate=%b",
            config.host, config.port,
            config.database, config.useSsl, config.useSsl
        ));
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setUsername(config.username);
        hikariConfig.setPassword(config.password);

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

        return MySqlProvider.of(new HikariDataSource(hikariConfig), config.database);
    }
}
