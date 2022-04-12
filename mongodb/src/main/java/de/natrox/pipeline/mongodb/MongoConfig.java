package de.natrox.pipeline.mongodb;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import de.natrox.common.builder.IBuilder;
import de.natrox.pipeline.config.part.PartConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class MongoConfig implements PartConfig<MongoProvider> {

    private final String host;
    private final int port;

    private final String authSource;
    private final String username;
    private final String password;

    private final String database;

    private final String overridingConnectionUri;

    private MongoConfig(
        @NotNull String host,
        int port,
        @Nullable String authSource,
        @Nullable String username,
        @Nullable String password,
        @NotNull String database,
        @Nullable String overridingConnectionUri
    ) {
        Preconditions.checkNotNull(host, "host");
        Preconditions.checkNotNull(database, "database");

        this.host = host;
        this.port = port;
        this.authSource = authSource;
        this.username = username;
        this.password = password;
        this.database = database;
        this.overridingConnectionUri = overridingConnectionUri;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public @NotNull String host() {
        return this.host;
    }

    public int port() {
        return this.port;
    }

    public String authSource() {
        return this.authSource;
    }

    public String username() {
        return this.username;
    }

    public String password() {
        return this.password;
    }

    public @NotNull String database() {
        return this.database;
    }

    public String overridingConnectionUri() {
        return this.overridingConnectionUri;
    }

    public @NotNull String buildConnectionUri() throws UnsupportedEncodingException {
        if (!Strings.isNullOrEmpty(this.overridingConnectionUri))
            return this.overridingConnectionUri;

        var authParams = Strings.isNullOrEmpty(this.username) && Strings.isNullOrEmpty(this.password)
            ? ""
            : String.format("%s:%s@", this.encodeUrl(this.username), this.encodeUrl(this.password));
        var authSource = Strings.isNullOrEmpty(this.authSource) ? "" : String.format("/?authSource=%s", this.authSource);

        return String.format("mongodb://%s%s:%d%s", authParams, this.host, this.port, authSource);
    }

    private String encodeUrl(String input) throws UnsupportedEncodingException {
        return URLEncoder.encode(input, StandardCharsets.UTF_8.name());
    }

    @Override
    public @NotNull MongoProvider createProvider() {
        return new MongoProvider(this);
    }

    public static class Builder implements IBuilder<MongoConfig> {

        private String host;
        private int port;

        private String authSource;
        private String username;
        private String password;

        private String database;

        private String overridingConnectionUri;

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

        public @NotNull Builder authSource(@Nullable String authSource) {
            this.authSource = authSource;
            return this;
        }

        public @NotNull Builder username(@Nullable String username) {
            this.username = username;
            return this;
        }

        public @NotNull Builder password(@Nullable String password) {
            this.password = password;
            return this;
        }

        public @NotNull Builder database(@NotNull String database) {
            Preconditions.checkNotNull(database, "database");
            this.database = database;
            return this;
        }

        public @NotNull Builder overridingConnectionUri(@Nullable String overridingConnectionUri) {
            this.overridingConnectionUri = overridingConnectionUri;
            return this;
        }

        @Override
        public @NotNull MongoConfig build() {
            return new MongoConfig(host, port, authSource, username, password, database, overridingConnectionUri);
        }
    }
}
