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

import de.natrox.common.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MySqlConfig {

    String host;
    int port;
    String username;
    String password;
    String database;
    boolean useSsl;

    MySqlConfig() {

    }

    public static @NotNull MySqlConfig create() {
        return new MySqlConfig();
    }

    public MySqlConfig setHost(@NotNull String host) {
        Check.notNull(host, "host");
        this.host = host;
        return this;
    }

    public @NotNull MySqlConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public @NotNull MySqlConfig setUsername(@Nullable String username) {
        this.username = username;
        return this;
    }

    public @NotNull MySqlConfig setPassword(@Nullable String password) {
        this.password = password;
        return this;
    }

    public @NotNull MySqlConfig setDatabase(@NotNull String database) {
        Check.notNull(database, "database");
        this.database = database;
        return this;
    }

    public @NotNull MySqlConfig setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
        return this;
    }
}
