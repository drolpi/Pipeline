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

package de.natrox.pipeline.repository;

import de.natrox.common.builder.IBuilder;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.config.StorageConfig;
import de.natrox.pipeline.part.provider.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.UnaryOperator;

@ApiStatus.Experimental
public sealed interface Pipeline permits AbstractPipeline {

    static @NotNull Pipeline.GlobalBuilder builder(@NotNull GlobalStorageProvider provider) {
        Check.notNull(provider, "provider");
        return new GlobalPipeline.Builder(provider);
    }

    static @NotNull Pipeline.LocalBuilder builder(@NotNull LocalStorageProvider provider) {
        Check.notNull(provider, "provider");
        return new LocalPipeline.Builder(provider);
    }

    @NotNull Repository repository(@NotNull String name);

    @NotNull Repository.Builder buildRepository(@NotNull String name, @NotNull StorageConfig config);

    default @NotNull Repository.Builder buildRepository(@NotNull String name, @NotNull StorageConfig.Builder builder) {
        Check.notNull(name, "name");
        Check.notNull(builder, "builder");
        return this.buildRepository(name, builder.build());
    }

    default @NotNull Repository.Builder buildRepository(@NotNull String name, @NotNull UnaryOperator<StorageConfig.Builder> function) {
        Check.notNull(name, "name");
        Check.notNull(function, "function");
        return this.buildRepository(name, function.apply(StorageConfig.builder()));
    }

    default @NotNull Repository.Builder buildRepository(@NotNull String name) {
        Check.notNull(name, "name");
        return this.buildRepository(name, StorageConfig.defaults());
    }

    boolean hasRepository(@NotNull String name);

    void destroyRepository(@NotNull String name);

    @NotNull Set<String> repositories();

    boolean isClosed();

    void close();

    void closeAll();

    interface Builder<R extends Builder<R>> extends IBuilder<Pipeline> {

        @NotNull R globalCache(@NotNull GlobalCacheProvider provider);

    }

    interface GlobalBuilder extends Builder<GlobalBuilder> {

        @NotNull Pipeline.GlobalBuilder localCache(@NotNull LocalCacheProvider localCacheProvider, @NotNull UpdaterProvider updaterProvider);

    }

    interface LocalBuilder extends Builder<LocalBuilder> {

        @NotNull Pipeline.LocalBuilder localCache(@NotNull LocalCacheProvider provider);

    }
}
