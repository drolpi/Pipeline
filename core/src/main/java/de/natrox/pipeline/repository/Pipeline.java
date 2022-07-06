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
import de.natrox.common.function.SingleTypeFunction;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.mapper.DocumentMapper;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.part.config.GlobalCacheConfig;
import de.natrox.pipeline.part.config.GlobalStorageConfig;
import de.natrox.pipeline.part.config.LocalCacheConfig;
import de.natrox.pipeline.part.config.LocalStorageConfig;
import de.natrox.pipeline.part.provider.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@ApiStatus.Experimental
public sealed interface Pipeline permits PipelineImpl {

    static @NotNull Pipeline.GlobalBuilder create(@NotNull GlobalStorageProvider provider, @NotNull GlobalStorageConfig config) {
        Check.notNull(provider, "provider");
        Check.notNull(config, "config");
        return new PipelineImpl.GlobalBuilderImpl(provider, config);
    }

    static @NotNull Pipeline.GlobalBuilder create(@NotNull GlobalStorageProvider provider, @NotNull GlobalStorageConfig.Builder builder) {
        Check.notNull(provider, "provider");
        Check.notNull(builder, "builder");
        return create(provider, builder.build());
    }

    static @NotNull Pipeline.GlobalBuilder create(@NotNull GlobalStorageProvider provider, @NotNull SingleTypeFunction<GlobalStorageConfig.Builder> function) {
        Check.notNull(provider, "provider");
        Check.notNull(function, "function");
        return create(provider, function.apply(GlobalStorageConfig.builder()));
    }

    static @NotNull Pipeline.GlobalBuilder create(@NotNull GlobalStorageProvider provider) {
        Check.notNull(provider, "provider");
        return create(provider, GlobalStorageConfig.defaults());
    }

    static @NotNull Pipeline.LocalBuilder create(@NotNull LocalStorageProvider provider, @NotNull LocalStorageConfig config) {
        Check.notNull(provider, "provider");
        Check.notNull(config, "config");
        return new PipelineImpl.LocalBuilderImpl(provider, config);
    }

    static @NotNull Pipeline.LocalBuilder create(@NotNull LocalStorageProvider provider, @NotNull LocalStorageConfig.Builder builder) {
        Check.notNull(provider, "provider");
        Check.notNull(builder, "builder");
        return create(provider, builder.build());
    }

    static @NotNull Pipeline.LocalBuilder create(@NotNull LocalStorageProvider provider, @NotNull SingleTypeFunction<LocalStorageConfig.Builder> function) {
        Check.notNull(provider, "provider");
        Check.notNull(function, "function");
        return create(provider, function.apply(LocalStorageConfig.builder()).build());
    }

    static @NotNull Pipeline.LocalBuilder create(@NotNull LocalStorageProvider provider) {
        Check.notNull(provider, "provider");
        return create(provider, LocalStorageConfig.defaults());
    }

    @NotNull DocumentRepository repository(@NotNull String name);

    @NotNull DocumentRepository.Builder buildRepository(@NotNull String name);

    <T extends ObjectData> @NotNull ObjectRepository<T> repository(@NotNull Class<T> type);

    <T extends ObjectData> ObjectRepository.@NotNull Builder<T> buildRepository(@NotNull Class<T> type);

    boolean hasRepository(@NotNull String name);

    <T> boolean hasRepository(@NotNull Class<T> type);

    void destroyRepository(@NotNull String name);

    <T extends ObjectData> void destroyRepository(@NotNull Class<T> type);

    @NotNull Set<String> repositories();

    @NotNull DocumentMapper documentMapper();

    boolean isClosed();

    void close();

    interface Builder<R extends Builder<R>> extends IBuilder<Pipeline> {

        @NotNull R globalCache(@NotNull GlobalCacheProvider provider, @NotNull GlobalCacheConfig config);

        default @NotNull R globalCache(@NotNull GlobalCacheProvider provider, @NotNull GlobalCacheConfig.Builder builder) {
            Check.notNull(provider, "provider");
            Check.notNull(builder, "builder");
            return this.globalCache(provider, builder.build());
        }

        default @NotNull R globalCache(@NotNull GlobalCacheProvider provider, @NotNull SingleTypeFunction<GlobalCacheConfig.Builder> function) {
            Check.notNull(provider, "provider");
            Check.notNull(function, "function");
            return this.globalCache(provider, function.apply(GlobalCacheConfig.builder()).build());
        }

        default @NotNull R globalCache(@NotNull GlobalCacheProvider provider) {
            Check.notNull(provider, "provider");
            return this.globalCache(provider, GlobalCacheConfig.defaults());
        }

    }

    interface GlobalBuilder extends Builder<GlobalBuilder> {

        @NotNull Pipeline.GlobalBuilder localCache(
            @NotNull LocalCacheProvider localCacheProvider,
            @NotNull UpdaterProvider updaterProvider,
            @NotNull LocalCacheConfig config
        );

        default @NotNull Pipeline.GlobalBuilder localCache(
            @NotNull LocalCacheProvider localCacheProvider,
            @NotNull UpdaterProvider updaterProvider,
            @NotNull LocalCacheConfig.Builder builder
        ) {
            Check.notNull(localCacheProvider, "localCacheProvider");
            Check.notNull(updaterProvider, "updaterProvider");
            Check.notNull(builder, "builder");
            return this.localCache(localCacheProvider, updaterProvider, builder.build());
        }

        default @NotNull Pipeline.GlobalBuilder localCache(
            @NotNull LocalCacheProvider localCacheProvider,
            @NotNull UpdaterProvider updaterProvider,
            @NotNull SingleTypeFunction<LocalCacheConfig.Builder> function
        ) {
            Check.notNull(localCacheProvider, "localCacheProvider");
            Check.notNull(updaterProvider, "updaterProvider");
            Check.notNull(function, "function");
            return this.localCache(localCacheProvider, updaterProvider, function.apply(LocalCacheConfig.builder()).build());
        }

        default @NotNull Pipeline.GlobalBuilder localCache(
            @NotNull LocalCacheProvider localCacheProvider,
            @NotNull UpdaterProvider updaterProvider
        ) {
            Check.notNull(localCacheProvider, "localCacheProvider");
            Check.notNull(updaterProvider, "updaterProvider");
            return this.localCache(localCacheProvider, updaterProvider, LocalCacheConfig.defaults());
        }
    }

    interface LocalBuilder extends Builder<LocalBuilder> {

        @NotNull Pipeline.LocalBuilder localCache(@NotNull LocalCacheProvider provider, @NotNull LocalCacheConfig localCacheConfig);

        default @NotNull Pipeline.LocalBuilder localCache(@NotNull LocalCacheProvider provider, @NotNull LocalCacheConfig.Builder builder) {
            Check.notNull(provider, "provider");
            Check.notNull(builder, "builder");
            return this.localCache(provider, builder.build());
        }

        default @NotNull Pipeline.LocalBuilder localCache(@NotNull LocalCacheProvider provider, @NotNull SingleTypeFunction<LocalCacheConfig.Builder> function) {
            Check.notNull(provider, "provider");
            Check.notNull(function, "function");
            return this.localCache(provider, function.apply(LocalCacheConfig.builder()).build());
        }

        default @NotNull Pipeline.LocalBuilder localCache(@NotNull LocalCacheProvider provider) {
            Check.notNull(provider, "provider");
            return this.localCache(provider, LocalCacheConfig.defaults());
        }
    }
}
