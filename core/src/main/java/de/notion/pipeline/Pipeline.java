package de.notion.pipeline;

import de.notion.common.system.SystemLoadable;
import de.notion.pipeline.config.PipelineConfig;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.filter.Filter;
import de.notion.pipeline.part.PipelineDataSynchronizer;
import de.notion.pipeline.part.cache.GlobalCache;
import de.notion.pipeline.part.local.LocalCache;
import de.notion.pipeline.part.local.updater.DataUpdaterService;
import de.notion.pipeline.part.storage.GlobalStorage;
import de.notion.pipeline.registry.PipelineRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Pipeline extends SystemLoadable {

    static Pipeline create(PipelineConfig config) {
        return create(config, new PipelineRegistry());
    }

    static Pipeline create(PipelineConfig config, PipelineRegistry registry) {
        return new PipelineManager(registry, config);
    }

    //Load by provided uuid
    @Nullable <T extends PipelineData> T load(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull LoadingStrategy loadingStrategy, @Nullable Consumer<T> callback, @NotNull QueryStrategy... creationStrategies);

    @NotNull <T extends PipelineData> CompletableFuture<T> loadAsync(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull LoadingStrategy loadingStrategy, @Nullable Consumer<T> callback, @NotNull QueryStrategy... creationStrategies);

    //Load by provided uuid without callback
    @Nullable
    default <T extends PipelineData> T load(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull LoadingStrategy loadingStrategy, @NotNull QueryStrategy... creationStrategies) {
        return load(type, uuid, loadingStrategy, null, creationStrategies);
    }

    @NotNull
    default <T extends PipelineData> CompletableFuture<T> loadAsync(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull LoadingStrategy loadingStrategy, @NotNull QueryStrategy... creationStrategies) {
        return loadAsync(type, uuid, loadingStrategy, null, creationStrategies);
    }

    //Load all data by filteredUUIDs
    @NotNull
    default <T extends PipelineData> List<T> load(@NotNull Class<? extends T> type, @NotNull Filter filter, @NotNull LoadingStrategy loadingStrategy) {
        return load(type, globalStorage().filteredUUIDs(type, filter), loadingStrategy);
    }

    //Maybe call filteredUUIDs async too?
    @NotNull
    default <T extends PipelineData> CompletableFuture<List<T>> loadAsync(@NotNull Class<? extends T> type, @NotNull Filter filter, @NotNull LoadingStrategy loadingStrategy) {
        return loadAsync(type, globalStorage().filteredUUIDs(type, filter), loadingStrategy);
    }

    //Load all data by filteredUUIDs
    //TODO: Sorter
    @NotNull
    default <T extends PipelineData> List<T> load(@NotNull Class<? extends T> type, @NotNull Object sorter, @NotNull LoadingStrategy loadingStrategy) {
        return load(type, globalStorage().sortedUUIDs(type), loadingStrategy);
    }

    //Maybe call sortedUUIDs async too?
    //TODO: Sorter
    @NotNull
    default <T extends PipelineData> CompletableFuture<List<T>> loadAsync(@NotNull Class<? extends T> type, @NotNull Object sorter, @NotNull LoadingStrategy loadingStrategy) {
        return loadAsync(type, globalStorage().sortedUUIDs(type), loadingStrategy);
    }

    //Load all data by provided uuids
    @NotNull <T extends PipelineData> List<T> load(@NotNull Class<? extends T> type, @NotNull List<UUID> uuids, @NotNull LoadingStrategy loadingStrategy);

    @NotNull <T extends PipelineData> CompletableFuture<List<T>> loadAsync(@NotNull Class<? extends T> type, @NotNull List<UUID> uuids, @NotNull LoadingStrategy loadingStrategy);

    //Load all data by saved uuids
    @NotNull <T extends PipelineData> List<T> load(@NotNull Class<? extends T> type, @NotNull LoadingStrategy loadingStrategy);

    @NotNull <T extends PipelineData> CompletableFuture<List<T>> loadAsync(@NotNull Class<? extends T> type, @NotNull LoadingStrategy loadingStrategy);

    <T extends PipelineData> boolean exist(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull QueryStrategy... strategies);

    @NotNull <T extends PipelineData> CompletableFuture<Boolean> existAsync(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull QueryStrategy... strategies);

    <T extends PipelineData> boolean delete(@NotNull Class<? extends T> type, @NotNull UUID uuid, boolean notifyOthers, @NotNull QueryStrategy... strategies);

    default <T extends PipelineData> boolean delete(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull QueryStrategy... strategies) {
        return delete(type, uuid, true, strategies);
    }

    default <T extends PipelineData> boolean delete(@NotNull Class<? extends T> type, @NotNull UUID uuid, boolean notifyOthers) {
        return delete(type, uuid, notifyOthers, QueryStrategy.ALL);
    }

    default <T extends PipelineData> boolean delete(@NotNull Class<? extends T> type, @NotNull UUID uuid) {
        return delete(type, uuid, true, QueryStrategy.ALL);
    }

    @NotNull <T extends PipelineData> CompletableFuture<Boolean> deleteAsync(@NotNull Class<? extends T> type, @NotNull UUID uuid, boolean notifyOthers, @NotNull QueryStrategy... strategies);

    @NotNull
    default <T extends PipelineData> CompletableFuture<Boolean> deleteAsync(@NotNull Class<? extends T> type, @NotNull UUID uuid, boolean notifyOthers) {
        return deleteAsync(type, uuid, notifyOthers, QueryStrategy.ALL);
    }

    @NotNull
    default <T extends PipelineData> CompletableFuture<Boolean> deleteAsync(@NotNull Class<? extends T> type, @NotNull UUID uuid) {
        return deleteAsync(type, uuid, true, QueryStrategy.ALL);
    }

    @NotNull LocalCache localCache();

    @NotNull DataUpdaterService dataUpdaterService();

    @Nullable GlobalCache globalCache();

    @Nullable GlobalStorage globalStorage();

    void saveAllData();

    void preloadAllData();

    void preloadData(@NotNull Class<? extends PipelineData> type);

    void preloadData(@NotNull Class<? extends PipelineData> type, @NotNull UUID uuid);

    void saveData(@NotNull Class<? extends PipelineData> type);

    void saveData(@NotNull Class<? extends PipelineData> type, @NotNull UUID uuid, Runnable callback);

    @NotNull
    PipelineDataSynchronizer dataSynchronizer();

    @NotNull
    PipelineRegistry registry();

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
}