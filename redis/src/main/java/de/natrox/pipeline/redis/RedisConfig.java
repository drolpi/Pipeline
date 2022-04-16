package de.natrox.pipeline.redis;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import de.natrox.common.builder.IBuilder;
import de.natrox.pipeline.config.part.PartConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public final class RedisConfig implements PartConfig<RedisProvider> {

    private final String username;
    private final String password;
    private final List<RedisEndpoint> endpoints;

    private RedisConfig(String username, String password, @NotNull List<RedisEndpoint> endpoints) {
        Preconditions.checkNotNull(endpoints, "endpoints");
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
            Preconditions.checkNotNull(endpoints, "endpoints");
            for (int i = 0, length = endpoints.length; i < length; i++) {
                var endpoint = endpoints[i];
                Preconditions.checkNotNull(endpoint, "alias at index %s", i);
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
