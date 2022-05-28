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

package de.natrox.pipeline.sqlite;

import de.natrox.common.builder.IBuilder;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.PartConfig;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class SQLiteConfig implements PartConfig<SQLiteProviderImpl> {

    private final String path;

    private SQLiteConfig(@NotNull Path path) {
        Check.notNull(path, "path");
        this.path = path.toAbsolutePath().toString();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public String path() {
        return this.path;
    }

    @Override
    public @NotNull SQLiteProviderImpl createProvider() {
        return new SQLiteProviderImpl(this);
    }

    public static class Builder implements IBuilder<SQLiteConfig> {

        private Path path;

        private Builder() {

        }

        public @NotNull Builder path(@NotNull Path path) {
            Check.notNull(path, "path");
            this.path = path;
            return this;
        }

        @Override
        public @NotNull SQLiteConfig build() {
            return new SQLiteConfig(this.path);
        }
    }

}
