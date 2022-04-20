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

package de.natrox.pipeline.h2;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.io.FileUtil;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import org.h2.Driver;
import org.h2.jdbcx.JdbcDataSource;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public final class H2Provider implements GlobalStorageProvider {

    static {
        Driver.load();
    }

    private final Path path;
    private final HikariDataSource hikariDataSource;

    protected H2Provider(@NotNull H2Config config) throws Exception {
        Preconditions.checkNotNull(config, "config");
        this.path = Path.of(config.path());

        var parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            FileUtil.createDirectory(parent);
        }
        var hikariConfig = new HikariConfig();

        var dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:file:" + path.toAbsolutePath());
        hikariConfig.setDataSource(dataSource);

        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(100);
        hikariConfig.setConnectionTimeout(10_000);
        hikariConfig.setValidationTimeout(10_000);

        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void shutdown() {
        if (hikariDataSource != null) {
            hikariDataSource.close();
        }
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new H2Storage(pipeline, hikariDataSource);
    }
}
