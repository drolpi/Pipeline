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
import de.natrox.pipeline.serialize.DocumentSerializer;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.part.config.StorageConfig;
import de.natrox.pipeline.part.provider.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@ApiStatus.Experimental
public sealed interface Pipeline permits AbstractPipeline {

    static @NotNull Pipeline.GlobalBuilder create(@NotNull GlobalStorageProvider provider) {
        Check.notNull(provider, "provider");
        return new GlobalPipeline.Builder(provider);
    }

    static @NotNull Pipeline.LocalBuilder create(@NotNull LocalStorageProvider provider) {
        Check.notNull(provider, "provider");
        return new LocalPipeline.Builder(provider);
    }

    @NotNull DocumentRepository repository(@NotNull String name);

    @NotNull DocumentRepository.Builder buildRepository(@NotNull String name, @NotNull StorageConfig config);

    default @NotNull DocumentRepository.Builder buildRepository(@NotNull String name, @NotNull StorageConfig.Builder builder) {
        Check.notNull(name, "name");
        Check.notNull(builder, "builder");
        return this.buildRepository(name, builder.build());
    }

    default @NotNull DocumentRepository.Builder buildRepository(@NotNull String name, @NotNull SingleTypeFunction<StorageConfig.Builder> function) {
        Check.notNull(name, "name");
        Check.notNull(function, "function");
        return this.buildRepository(name, function.apply(StorageConfig.builder()));
    }

    default @NotNull DocumentRepository.Builder buildRepository(@NotNull String name) {
        Check.notNull(name, "name");
        return this.buildRepository(name, StorageConfig.defaults());
    }

    <T extends ObjectData> @NotNull ObjectRepository<T> repository(@NotNull Class<T> type);

    <T extends ObjectData> ObjectRepository.@NotNull Builder<T> buildRepository(@NotNull Class<T> type, @NotNull StorageConfig config);

    default <T extends ObjectData> ObjectRepository.@NotNull Builder<T> buildRepository(@NotNull Class<T> type, @NotNull StorageConfig.Builder builder) {
        Check.notNull(type, "type");
        Check.notNull(builder, "builder");
        return this.buildRepository(type, builder.build());
    }

    default <T extends ObjectData> ObjectRepository.@NotNull Builder<T> buildRepository(@NotNull Class<T> type, @NotNull SingleTypeFunction<StorageConfig.Builder> function) {
        Check.notNull(type, "type");
        Check.notNull(function, "function");
        return this.buildRepository(type, function.apply(StorageConfig.builder()));
    }

    default <T extends ObjectData> ObjectRepository.@NotNull Builder<T> buildRepository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        return this.buildRepository(type, StorageConfig.defaults());
    }

    boolean hasRepository(@NotNull String name);

    <T> boolean hasRepository(@NotNull Class<T> type);

    void destroyRepository(@NotNull String name);

    <T extends ObjectData> void destroyRepository(@NotNull Class<T> type);

    @NotNull Set<String> repositories();

    @NotNull DocumentSerializer documentMapper();

    boolean isClosed();

    void close();

    void closeProviders();

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
