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

package de.natrox.pipeline.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.io.FileUtil;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.Store;
import de.natrox.pipeline.part.provider.GlobalStorageProvider;
import de.natrox.pipeline.sql.SqlStore;
import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public final class SQLiteProvider implements GlobalStorageProvider {

    private final HikariDataSource hikariDataSource;

    SQLiteProvider(@NotNull SQLiteConfig config) {
        Check.notNull(config, "config");
        Path path = Path.of(config.path());
        Path parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            FileUtil.createDirectory(parent);
        }
        HikariConfig hikariConfig = new HikariConfig();

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + path.toAbsolutePath());
        hikariConfig.setDataSource(dataSource);

        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(100);
        hikariConfig.setConnectionTimeout(10_000);
        hikariConfig.setValidationTimeout(10_000);

        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void close() {
        this.hikariDataSource.close();
    }

    @Override
    public @NotNull Store createGlobalStorage(@NotNull Pipeline pipeline) {
        return new SQLiteStore(this.hikariDataSource);
    }
}
