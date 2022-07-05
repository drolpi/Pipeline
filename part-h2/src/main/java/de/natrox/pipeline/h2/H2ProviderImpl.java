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

package de.natrox.pipeline.h2;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.io.FileUtil;
import de.natrox.pipeline.repository.Pipeline;
import de.natrox.pipeline.part.config.GlobalStorageConfig;
import de.natrox.pipeline.part.store.Store;
import org.h2.Driver;
import org.h2.jdbcx.JdbcDataSource;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

final class H2ProviderImpl implements H2Provider {

    static {
        Driver.load();
    }

    private final HikariDataSource hikariDataSource;

    H2ProviderImpl(@NotNull Path directory) {
        Path parent = directory.getParent();

        if (parent != null && !Files.exists(parent)) {
            FileUtil.createDirectory(parent);
        }
        HikariConfig hikariConfig = new HikariConfig();

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:file:" + directory.toAbsolutePath());
        hikariConfig.setDataSource(dataSource);

        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(100);
        hikariConfig.setConnectionTimeout(10_000);
        hikariConfig.setValidationTimeout(10_000);

        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void close() {
        if (this.hikariDataSource != null) {
            this.hikariDataSource.close();
        }
    }

    @Override
    public @NotNull Store createGlobalStorage(@NotNull Pipeline pipeline, @NotNull GlobalStorageConfig config) {
        return new H2Store(this.hikariDataSource);
    }
}
