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

package de.natrox.pipeline.mongo;

import com.google.common.base.Strings;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.exception.PartException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class MongoConfig {

    String database;
    private String host;
    private int port;
    private String authSource;
    private String username;
    private String password;

    MongoConfig() {

    }

    public static @NotNull MongoConfig create() {
        return new MongoConfig();
    }

    public @NotNull MongoConfig setHost(@NotNull String host) {
        Check.notNull(host, "host");
        this.host = host;
        return this;
    }

    public @NotNull MongoConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public @NotNull MongoConfig setAuthSource(@Nullable String authSource) {
        this.authSource = authSource;
        return this;
    }

    public @NotNull MongoConfig setUsername(@Nullable String username) {
        this.username = username;
        return this;
    }

    public @NotNull MongoConfig setPassword(@Nullable String password) {
        this.password = password;
        return this;
    }

    public @NotNull MongoConfig setDatabase(@NotNull String database) {
        Check.notNull(database, "database");
        this.database = database;
        return this;
    }

    @NotNull String buildConnectionUri() {
        try {
            String authParams = Strings.isNullOrEmpty(this.username) && Strings.isNullOrEmpty(this.password)
                ? ""
                : String.format("%s:%s@", this.encodeUrl(this.username), this.encodeUrl(this.password));
            String authSource = Strings.isNullOrEmpty(this.authSource) ? "" : String.format("/?authSource=%s", this.authSource);

            return String.format("mongodb://%s%s:%d%s", authParams, this.host, this.port, authSource);
        } catch (UnsupportedEncodingException e) {
            //TODO: message/reason
            throw new PartException("", e);
        }
    }

    private String encodeUrl(String input) throws UnsupportedEncodingException {
        return URLEncoder.encode(input, StandardCharsets.UTF_8.name());
    }
}
