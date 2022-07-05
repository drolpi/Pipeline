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

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.pipeline.repository.Pipeline;
import de.natrox.pipeline.part.config.GlobalStorageConfig;
import de.natrox.pipeline.part.store.Store;
import de.natrox.pipeline.sql.SqlStore;
import org.jetbrains.annotations.NotNull;

public final class MySqlProviderImpl implements MySqlProvider {

    private final HikariDataSource hikariDataSource;

    MySqlProviderImpl(@NotNull HikariDataSource hikariDataSource, @NotNull String databaseName) {
        this.hikariDataSource = hikariDataSource;
    }

    @Override
    public void close() {
        this.hikariDataSource.close();
    }

    @Override
    public @NotNull Store createGlobalStorage(@NotNull Pipeline pipeline, @NotNull GlobalStorageConfig config) {
        return new SqlStore(this.hikariDataSource);
    }
}
