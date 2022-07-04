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

import de.natrox.common.function.SingleTypeFunction;
import de.natrox.common.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class RedisConfig {

    final List<RedisEndpoint> endpoints;
    String username;
    String password;

    RedisConfig() {
        this.endpoints = new ArrayList<>();
    }

    public static @NotNull RedisConfig create() {
        return new RedisConfig();
    }

    public @NotNull RedisConfig setUsername(@Nullable String username) {
        this.username = username;
        return this;
    }

    public @NotNull RedisConfig setPassword(@Nullable String password) {
        this.password = password;
        return this;
    }

    public @NotNull RedisConfig addEndpoints(RedisEndpoint @NotNull ... endpoints) {
        Check.notNull(endpoints, "endpoints");
        for (int i = 0, length = endpoints.length; i < length; i++) {
            RedisEndpoint endpoint = endpoints[i];
            Check.notNull(endpoint, "alias at index %s", i);
            this.endpoints.add(endpoint);
        }
        return this;
    }

    public @NotNull RedisConfig addEndpoint(@NotNull SingleTypeFunction<RedisEndpoint> function) {
        Check.notNull(function, "function");
        return this.addEndpoints(function.apply(RedisEndpoint.create()));
    }
}
