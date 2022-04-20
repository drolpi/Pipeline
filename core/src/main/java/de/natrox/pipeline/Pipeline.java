/*
 * Copyright 2020-2022 NatroxMC team
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

import de.natrox.common.Loadable;
import de.natrox.common.Shutdownable;
import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import de.natrox.pipeline.json.JsonProvider;
import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.json.serializer.PipelineDataSerializer;
import de.natrox.pipeline.operator.PipelineStream;
import de.natrox.pipeline.part.DataSynchronizer;
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.cache.GlobalCacheProvider;
import de.natrox.pipeline.part.local.LocalCache;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import de.natrox.pipeline.part.updater.DataUpdater;
import de.natrox.pipeline.part.updater.DataUpdaterProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Pipeline extends Loadable, Shutdownable {

    static @NotNull Builder builder() {
        return new PipelineBuilder();
    }

    <T extends PipelineData> @NotNull PipelineStream<T> find(
        @NotNull Class<? extends T> type,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    );

    // Without callback
    default <T extends PipelineData> @NotNull PipelineStream<T> find(
        @NotNull Class<? extends T> type,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        return find(type, null, instanceCreator);
    }

    // Without instance creator
    default <T extends PipelineData> @NotNull PipelineStream<T> find(
        @NotNull Class<? extends T> type,
        @Nullable Consumer<T> callback
    ) {
        return find(type, callback, null);
    }

    // Without callback & instance creator
    default <T extends PipelineData> @NotNull PipelineStream<T> find(
        @NotNull Class<? extends T> type
    ) {
        return find(type, null, null);
    }

    //Load by provided uuid
    @NotNull <T extends PipelineData> Optional<T> load(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator,
        boolean createIfNotExists
    );

    //Without createIfNotExists
    default <T extends PipelineData> @NotNull Optional<T> load(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        return load(type, uuid, loadingStrategy, callback, instanceCreator, false);
    }

    @NotNull <T extends PipelineData> CompletableFuture<Optional<T>> loadAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator,
        boolean createIfNotExists
    );

    default <T extends PipelineData> @NotNull CompletableFuture<Optional<T>> loadAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        return loadAsync(type, uuid, loadingStrategy, callback, instanceCreator, false);
    }

    //Load by provided uuid without instance creator
    default <T extends PipelineData> @NotNull Optional<T> load(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        boolean createIfNotExists
    ) {
        return load(type, uuid, loadingStrategy, callback, null, createIfNotExists);
    }

    //Without createIfNotExists
    default <T extends PipelineData> @NotNull Optional<T> load(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback
    ) {
        return load(type, uuid, loadingStrategy, callback, null, false);
    }

    default <T extends PipelineData> @NotNull CompletableFuture<Optional<T>> loadAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        boolean createIfNotExists
    ) {
        return loadAsync(type, uuid, loadingStrategy, callback, null, createIfNotExists);
    }

    default <T extends PipelineData> @NotNull CompletableFuture<Optional<T>> loadAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback
    ) {
        return loadAsync(type, uuid, loadingStrategy, callback, null, false);
    }

    //Load by provided uuid without callback
    default <T extends PipelineData> @NotNull Optional<T> load(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable InstanceCreator<T> instanceCreator,
        boolean createIfNotExists
    ) {
        return load(type, uuid, loadingStrategy, null, instanceCreator, createIfNotExists);
    }

    default <T extends PipelineData> @NotNull Optional<T> load(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        return load(type, uuid, loadingStrategy, null, instanceCreator, false);
    }

    default <T extends PipelineData> @NotNull CompletableFuture<Optional<T>> loadAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable InstanceCreator<T> instanceCreator,
        boolean createIfNotExists
    ) {
        return loadAsync(type, uuid, loadingStrategy, null, instanceCreator, createIfNotExists);
    }

    default <T extends PipelineData> @NotNull CompletableFuture<Optional<T>> loadAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        return loadAsync(type, uuid, loadingStrategy, null, instanceCreator, false);
    }

    //Load by provided uuid without callback & instance creator
    default <T extends PipelineData> @NotNull Optional<T> load(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        boolean createIfNotExists
    ) {
        return load(type, uuid, loadingStrategy, null, null, createIfNotExists);
    }

    default <T extends PipelineData> @NotNull Optional<T> load(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy
    ) {
        return load(type, uuid, loadingStrategy, null, null, false);
    }

    default <T extends PipelineData> @NotNull CompletableFuture<Optional<T>> loadAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        boolean createIfNotExists
    ) {
        return loadAsync(type, uuid, loadingStrategy, null, null, createIfNotExists);
    }

    default <T extends PipelineData> @NotNull CompletableFuture<Optional<T>> loadAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy
    ) {
        return loadAsync(type, uuid, loadingStrategy, null, null, false);
    }

    //Load all data by provided uuids
    <T extends PipelineData> @NotNull List<T> load(
        @NotNull Class<? extends T> type,
        @NotNull Iterable<UUID> uuids,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    );

    <T extends PipelineData> @NotNull CompletableFuture<List<T>> loadAsync(
        @NotNull Class<? extends T> type,
        @NotNull Iterable<UUID> uuids,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    );

    // Without callback & instance creator
    default <T extends PipelineData> @NotNull List<T> load(
        @NotNull Class<? extends T> type,
        @NotNull Iterable<UUID> uuids,
        @NotNull LoadingStrategy loadingStrategy
    ) {
        return load(type, uuids, loadingStrategy, null, null);
    }

    default <T extends PipelineData> @NotNull CompletableFuture<List<T>> loadAsync(
        @NotNull Class<? extends T> type,
        @NotNull Iterable<UUID> uuids,
        @NotNull LoadingStrategy loadingStrategy
    ) {
        return loadAsync(type, uuids, loadingStrategy, null, null);
    }

    <T extends PipelineData> boolean exist(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull QueryStrategy... strategies
    );

    <T extends PipelineData> @NotNull CompletableFuture<Boolean> existAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull QueryStrategy... strategies
    );

    <T extends PipelineData> boolean delete(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        boolean notifyOthers,
        @NotNull QueryStrategy... strategies
    );

    default <T extends PipelineData> boolean delete(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull QueryStrategy... strategies
    ) {
        return delete(type, uuid, true, strategies);
    }

    default <T extends PipelineData> boolean delete(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid, boolean notifyOthers
    ) {
        return delete(type, uuid, notifyOthers, QueryStrategy.ALL);
    }

    default <T extends PipelineData> boolean delete(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid
    ) {
        return delete(type, uuid, true, QueryStrategy.ALL);
    }

    <T extends PipelineData> @NotNull CompletableFuture<Boolean> deleteAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid, boolean notifyOthers,
        @NotNull QueryStrategy... strategies
    );

    default <T extends PipelineData> @NotNull CompletableFuture<Boolean> deleteAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        boolean notifyOthers
    ) {
        return deleteAsync(type, uuid, notifyOthers, QueryStrategy.ALL);
    }

    default <T extends PipelineData> @NotNull CompletableFuture<Boolean> deleteAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid
    ) {
        return deleteAsync(type, uuid, true, QueryStrategy.ALL);
    }

    @NotNull Collection<UUID> keys(@NotNull Class<? extends PipelineData> dataClass);

    @NotNull Collection<JsonDocument> documents(@NotNull Class<? extends PipelineData> dataClass);

    @NotNull Map<UUID, JsonDocument> entries(@NotNull Class<? extends PipelineData> dataClass);

    @NotNull LocalCache localCache();

    @NotNull DataUpdater dataUpdater();

    @Nullable GlobalCache globalCache();

    @Nullable GlobalStorage globalStorage();

    void preloadAllData();

    void preloadData(@NotNull Class<? extends PipelineData> type);

    void preloadData(@NotNull Class<? extends PipelineData> type, @NotNull UUID uuid);

    void cleanUpAllData();

    void cleanUpData(@NotNull Class<? extends PipelineData> type);

    void cleanUpData(@NotNull Class<? extends PipelineData> type, @NotNull UUID uuid, Runnable callback);

    @NotNull DataSynchronizer dataSynchronizer();

    @NotNull PipelineRegistry registry();

    @NotNull JsonDocument.Factory documentFactory();

    @NotNull PipelineDataSerializer.Factory serializerFactory();

    enum LoadingStrategy {
        // Data will be loaded from Local Cache
        LOAD_LOCAL,
        // Data will be loaded from local Cache if not cached it will be loaded into local cache async for the next possible try
        LOAD_LOCAL_ELSE_LOAD,
        // Loads data from PipeLine
        LOAD_PIPELINE
    }

    enum QueryStrategy {
        // Instruction will be executed for Local Cache
        LOCAL,
        // Instruction will be executed for Global Cache
        GLOBAL_CACHE,
        // Instruction will be executed for Global Storage
        GLOBAL_STORAGE,
        // Instruction will be executed for all
        ALL
    }

    interface Builder {

        @NotNull Builder registry(@NotNull PipelineRegistry registry);

        @NotNull Builder jsonProvider(@NotNull JsonProvider jsonProvider);

        @NotNull Builder dataUpdater(@UnknownNullability DataUpdaterProvider connection);

        @NotNull Builder globalCache(@UnknownNullability GlobalCacheProvider connection);

        @NotNull Builder globalStorage(@UnknownNullability GlobalStorageProvider connection);

        @NotNull Pipeline build();

    }
}
