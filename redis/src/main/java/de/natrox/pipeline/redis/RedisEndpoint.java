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

package de.natrox.pipeline.redis;

import de.natrox.common.builder.IBuilder;
import de.natrox.common.validate.Check;
import org.jetbrains.annotations.NotNull;

public final class RedisEndpoint {

    private final String host;
    private final int port;
    private final int database;

    private RedisEndpoint(@NotNull String host, int port, int database) {
        Check.notNull(host, "host");
        this.host = host;
        this.port = port;
        this.database = database;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String host() {
        return this.host;
    }

    public int port() {
        return this.port;
    }

    public int database() {
        return this.database;
    }

    public static class Builder implements IBuilder<RedisEndpoint> {

        private String host;
        private int port;
        private int database;

        private Builder() {

        }

        public @NotNull Builder host(@NotNull String host) {
            Check.notNull(host, "host");
            this.host = host;
            return this;
        }

        public @NotNull Builder port(int port) {
            this.port = port;
            return this;
        }

        public @NotNull Builder database(int database) {
            this.database = database;
            return this;
        }

        @Override
        public @NotNull RedisEndpoint build() {
            return new RedisEndpoint(host, port, database);
        }
    }

}
