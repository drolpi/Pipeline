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
import de.natrox.pipeline.exception.PartException;
import de.natrox.pipeline.part.PartConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class RedisConfig implements PartConfig<RedisProvider> {

    final List<RedisEndpoint> endpoints;
    String username;
    String password;

    RedisConfig() {
        this.endpoints = new ArrayList<>();
    }

    public @NotNull RedisConfig username(@Nullable String username) {
        this.username = username;
        return this;
    }

    public @NotNull RedisConfig password(@Nullable String password) {
        this.password = password;
        return this;
    }

    public @NotNull RedisConfig endpoints(RedisEndpoint @NotNull ... endpoints) {
        Check.notNull(endpoints, "endpoints");
        for (int i = 0, length = endpoints.length; i < length; i++) {
            RedisEndpoint endpoint = endpoints[i];
            Check.notNull(endpoint, "alias at index %s", i);
            this.endpoints.add(endpoint);
        }
        return this;
    }

    public @NotNull RedisConfig endpoint(@NotNull Consumer<RedisEndpoint> consumer) {
        Check.notNull(consumer, "consumer");
        RedisEndpoint endpoint = RedisEndpoint.create();
        consumer.accept(endpoint);
        return this.endpoints(endpoint);
    }

    @Override
    public @NotNull RedisProvider buildProvider() {
        try {
            return RedisProvider.of(this);
        } catch (Exception exception) {
            throw new PartException("Failed to create RedisProvider", exception);
        }
    }
}
