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

package de.natrox.pipeline;

import de.natrox.common.builder.IBuilder;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.document.DocumentRepository;
import de.natrox.pipeline.document.option.DocumentOptions;
import de.natrox.pipeline.mapper.DocumentMapper;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.object.ObjectRepository;
import de.natrox.pipeline.object.option.ObjectOptions;
import de.natrox.pipeline.part.config.GlobalCacheConfig;
import de.natrox.pipeline.part.config.GlobalStorageConfig;
import de.natrox.pipeline.part.config.LocalCacheConfig;
import de.natrox.pipeline.part.config.LocalStorageConfig;
import de.natrox.pipeline.part.provider.GlobalCacheProvider;
import de.natrox.pipeline.part.provider.GlobalStorageProvider;
import de.natrox.pipeline.part.provider.LocalCacheProvider;
import de.natrox.pipeline.part.provider.LocalStorageProvider;
import de.natrox.pipeline.part.provider.UpdaterProvider;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Consumer;

@ApiStatus.Experimental
public sealed interface Pipeline permits PipelineImpl {

    static @NotNull Pipeline.GlobalBuilder create(@NotNull GlobalStorageProvider provider, @NotNull GlobalStorageConfig config) {
        Check.notNull(provider, "provider");
        Check.notNull(config, "config");
        return new PipelineBuilderImpl.GlobalBuilderImpl(provider, config);
    }

    static @NotNull Pipeline.GlobalBuilder create(@NotNull GlobalStorageProvider provider, @NotNull Consumer<GlobalStorageConfig> consumer) {
        Check.notNull(provider, "provider");
        Check.notNull(consumer, "consumer");
        GlobalStorageConfig config = GlobalStorageConfig.create();
        consumer.accept(config);
        return create(provider, config);
    }

    static @NotNull Pipeline.LocalBuilder create(@NotNull LocalStorageProvider provider, @NotNull LocalStorageConfig config) {
        Check.notNull(provider, "provider");
        Check.notNull(config, "config");
        return new PipelineBuilderImpl.LocalBuilderImpl(provider, config);
    }

    static @NotNull Pipeline.LocalBuilder create(@NotNull LocalStorageProvider provider, @NotNull Consumer<LocalStorageConfig> consumer) {
        Check.notNull(provider, "provider");
        Check.notNull(consumer, "consumer");
        LocalStorageConfig config = LocalStorageConfig.create();
        consumer.accept(config);
        return create(provider, config);
    }

    @NotNull DocumentRepository repository(@NotNull String name, @NotNull DocumentOptions options);

    default @NotNull DocumentRepository repository(@NotNull String name, @NotNull Consumer<DocumentOptions.Builder> consumer) {
        DocumentOptions.Builder builder = DocumentOptions.builder();
        consumer.accept(builder);
        return this.repository(name, builder.build());
    }

    default @NotNull DocumentRepository repository(@NotNull String name) {
        return this.repository(name, DocumentOptions.DEFAULT);
    }

    <T extends ObjectData> @NotNull ObjectRepository<T> repository(@NotNull Class<T> type, @NotNull ObjectOptions<T> options);

    default <T extends ObjectData> @NotNull ObjectRepository<T> repository(@NotNull Class<T> type, @NotNull Consumer<ObjectOptions.Builder<T>> consumer) {
        ObjectOptions.Builder<T> builder = ObjectOptions.of(type);
        consumer.accept(builder);
        return this.repository(type, builder.build());
    }

    default <T extends ObjectData> @NotNull ObjectRepository<T> repository(@NotNull Class<T> type) {
        return this.repository(type, ObjectOptions.of(type).build());
    }

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

        default @NotNull R globalCache(@NotNull GlobalCacheProvider provider, @NotNull Consumer<GlobalCacheConfig> consumer) {
            GlobalCacheConfig config = GlobalCacheConfig.create();
            consumer.accept(config);
            return this.globalCache(provider, config);
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
            @NotNull Consumer<LocalCacheConfig> consumer
        ) {
            LocalCacheConfig config = LocalCacheConfig.create();
            consumer.accept(config);
            return this.localCache(localCacheProvider, updaterProvider, config);
        }

    }

    interface LocalBuilder extends Builder<LocalBuilder> {

        @NotNull Pipeline.LocalBuilder localCache(@NotNull LocalCacheProvider provider, @NotNull LocalCacheConfig localCacheConfig);

        default @NotNull Pipeline.LocalBuilder localCache(@NotNull LocalCacheProvider provider, @NotNull Consumer<LocalCacheConfig> consumer) {
            LocalCacheConfig config = LocalCacheConfig.create();
            consumer.accept(config);
            return this.localCache(provider, config);
        }

    }

}
