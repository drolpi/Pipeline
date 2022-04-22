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

import de.natrox.common.validate.Check;
import com.google.common.collect.ImmutableList;
import de.natrox.common.builder.IBuilder;
import de.natrox.pipeline.old.config.part.PartConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public final class RedisConfig implements PartConfig<RedisProvider> {

    private final String username;
    private final String password;
    private final List<RedisEndpoint> endpoints;

    private RedisConfig(String username, String password, @NotNull List<RedisEndpoint> endpoints) {
        Check.notNull(endpoints, "endpoints");
        this.username = username;
        this.password = password;
        this.endpoints = endpoints;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String username() {
        return this.username;
    }

    public String password() {
        return this.password;
    }

    public List<RedisEndpoint> endpoints() {
        return this.endpoints;
    }

    @Override
    public @NotNull RedisProvider createProvider() throws Exception {
        return new RedisProvider(this);
    }

    public static class Builder implements IBuilder<RedisConfig> {

        private final ImmutableList.Builder<RedisEndpoint> endpoints;
        private String username;
        private String password;

        private Builder() {
            this.endpoints = new ImmutableList.Builder<>();
        }

        public Builder username(@Nullable String username) {
            this.username = username;
            return this;
        }

        public Builder password(@Nullable String password) {
            this.password = password;
            return this;
        }

        public @NotNull Builder endpoints(RedisEndpoint... endpoints) {
            Check.notNull(endpoints, "endpoints");
            for (int i = 0, length = endpoints.length; i < length; i++) {
                var endpoint = endpoints[i];
                Check.notNull(endpoint, "alias at index %s", i);
                this.endpoints.add(endpoint);
            }
            return this;
        }

        @Override
        public @NotNull RedisConfig build() {
            return new RedisConfig(username, password, endpoints.build());
        }
    }
}
