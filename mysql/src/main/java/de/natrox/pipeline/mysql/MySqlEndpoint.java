package de.natrox.pipeline.mysql;

import com.google.common.base.Preconditions;
import de.natrox.common.builder.IBuilder;
import org.jetbrains.annotations.NotNull;

public final class MySqlEndpoint {

    private final String host;
    private final int port;
    private final String database;
    private final boolean useSsl;

    private MySqlEndpoint(@NotNull String host, int port, @NotNull String database, boolean useSsl) {
        Preconditions.checkNotNull(host, "host");
        Preconditions.checkNotNull(database, "database");

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
            Preconditions.checkNotNull(host, "host");
            this.host = host;
            return this;
        }

        public @NotNull Builder port(int port) {
            this.port = port;
            return this;
        }

        public @NotNull Builder database(@NotNull String database) {
            Preconditions.checkNotNull(database, "database");
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
