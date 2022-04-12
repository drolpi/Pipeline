package de.natrox.pipeline.mysql;

import com.google.common.base.Preconditions;
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
        Preconditions.checkNotNull(endpoints, "endpoints");
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
    public @NotNull MySqlProvider createProvider() {
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
            Preconditions.checkNotNull(endpoints, "endpoints");
            for (int i = 0, length = endpoints.length; i < length; i++) {
                var endpoint = endpoints[i];
                Preconditions.checkNotNull(endpoint, "alias at index %s", i);
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
