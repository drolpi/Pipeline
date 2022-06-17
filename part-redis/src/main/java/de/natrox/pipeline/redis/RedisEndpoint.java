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

package de.natrox.pipeline.redis;

import de.natrox.common.validate.Check;
import org.jetbrains.annotations.NotNull;

public final class RedisEndpoint {

    String host;
    int port;
    int database;

    RedisEndpoint() {

    }

    public static @NotNull RedisEndpoint create() {
        return new RedisEndpoint();
    }

    public @NotNull RedisEndpoint host(@NotNull String host) {
        Check.notNull(host, "host");
        this.host = host;
        return this;
    }

    public @NotNull RedisEndpoint port(int port) {
        this.port = port;
        return this;
    }

    public @NotNull RedisEndpoint database(int database) {
        this.database = database;
        return this;
    }

}
