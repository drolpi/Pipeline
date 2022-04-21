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
import com.google.common.collect.ImmutableList;
import de.natrox.common.builder.IBuilder;
import de.natrox.pipeline.config.part.PartConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MySqlConfig implements PartConfig<MySqlProvider> {

    private final String username;
    private final String password;
    private final List<MySqlEndpoint> endpoints;

    private MySqlConfig(String username, String password, @NotNull List<MySqlEndpoint> endpoints) {
        Check.notNull(endpoints, "endpoints");
        this.username = username;
        this.password = password;
        this.endpoints = endpoints;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public String username() {
        return this.username;
    }

    public String password() {
        return this.password;
    }

    public @NotNull List<MySqlEndpoint> endpoints() {
        return this.endpoints;
    }

    public @NotNull MySqlEndpoint randomEndpoint() {
        if (this.endpoints.isEmpty())
            throw new IllegalStateException("No mysql connection endpoints available");

        return this.endpoints.get(ThreadLocalRandom.current().nextInt(0, this.endpoints.size()));
    }

    @Override
    public @NotNull MySqlProvider createProvider() throws Exception {
        return new MySqlProvider(this);
    }

    public static class Builder implements IBuilder<MySqlConfig> {

        private final ImmutableList.Builder<MySqlEndpoint> endpoints;
        private String username;
        private String password;

        public Builder() {
            this.endpoints = new ImmutableList.Builder<>();
        }

        public @NotNull Builder username(@Nullable String username) {
            this.username = username;
            return this;
        }

        public @NotNull Builder password(@Nullable String password) {
            this.password = password;
            return this;
        }

        public @NotNull Builder endpoints(MySqlEndpoint @NotNull ... endpoints) {
            Check.notNull(endpoints, "endpoints");
            for (int i = 0, length = endpoints.length; i < length; i++) {
                var endpoint = endpoints[i];
                Check.notNull(endpoint, "alias at index %s", i);
                this.endpoints.add(endpoint);
            }
            return this;
        }

        @Override
        public @NotNull MySqlConfig build() {
            return new MySqlConfig(username, password, endpoints.build());
        }
    }

}
