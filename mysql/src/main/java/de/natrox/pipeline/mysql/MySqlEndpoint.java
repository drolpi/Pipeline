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
import de.natrox.common.builder.IBuilder;
import org.jetbrains.annotations.NotNull;

public final class MySqlEndpoint {

    private final String host;
    private final int port;
    private final String database;
    private final boolean useSsl;

    private MySqlEndpoint(@NotNull String host, int port, @NotNull String database, boolean useSsl) {
        Check.notNull(host, "host");
        Check.notNull(database, "database");

        this.host = host;
        this.port = port;
        this.database = database;
        this.useSsl = useSsl;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public String host() {
        return this.host;
    }

    public int port() {
        return this.port;
    }

    public String database() {
        return this.database;
    }

    public boolean useSsl() {
        return this.useSsl;
    }

    public static class Builder implements IBuilder<MySqlEndpoint> {

        private String host;
        private int port;
        private String database;
        private boolean useSsl;

        private Builder() {

        }

        public Builder host(@NotNull String host) {
            Check.notNull(host, "host");
            this.host = host;
            return this;
        }

        public @NotNull Builder port(int port) {
            this.port = port;
            return this;
        }

        public @NotNull Builder database(@NotNull String database) {
            Check.notNull(database, "database");
            this.database = database;
            return this;
        }

        public @NotNull Builder useSsl(boolean useSsl) {
            this.useSsl = useSsl;
            return this;
        }

        @Override
        public @NotNull MySqlEndpoint build() {
            return new MySqlEndpoint(host, port, database, useSsl);
        }
    }

}
