package de.natrox.pipeline.redis;

import com.google.common.base.Preconditions;
import de.natrox.common.builder.IBuilder;
import org.jetbrains.annotations.NotNull;

public final class RedisEndpoint {

    private final String host;
    private final int port;
    private final int database;

    private RedisEndpoint(@NotNull String host, int port, int database) {
        Preconditions.checkNotNull(host, "host");
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
            Preconditions.checkNotNull(host, "host");
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
