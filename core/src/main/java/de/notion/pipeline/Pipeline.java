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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    @Nullable <T extends PipelineData> T load(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull LoadingStrategy loadingStrategy, @Nullable Consumer<T> callback, @NotNull QueryStrategy... creationStrategies);

    @NotNull <T extends PipelineData> CompletableFuture<T> loadAsync(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull LoadingStrategy loadingStrategy, @Nullable Consumer<T> callback, @NotNull QueryStrategy... creationStrategies);

    @Nullable
    default <T extends PipelineData> T load(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull LoadingStrategy loadingStrategy, @NotNull QueryStrategy... creationStrategies) {
        return load(type, uuid, loadingStrategy, null, creationStrategies);
    }

    @NotNull
    default <T extends PipelineData> CompletableFuture<T> loadAsync(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull LoadingStrategy loadingStrategy, @NotNull QueryStrategy... creationStrategies) {
        return loadAsync(type, uuid, loadingStrategy, null, creationStrategies);
    }

    @NotNull
    default <T extends PipelineData> List<T> load(@NotNull Class<? extends T> type, @NotNull Filter filter, @NotNull LoadingStrategy loadingStrategy, @NotNull QueryStrategy... creationStrategies) {
        List<UUID> uuids = globalStorage().filteredUUIDs(type, filter);
        List<T> data = new ArrayList<>();

        for (UUID uuid : uuids) {
            data.add(load(type, uuid, loadingStrategy, creationStrategies));
        }

        return data;
    }

    @NotNull
    default <T extends PipelineData> CompletableFuture<List<T>> loadAsync(@NotNull Class<? extends T> type, @NotNull Filter filter, @NotNull LoadingStrategy loadingStrategy, @NotNull QueryStrategy... creationStrategies) {
        CompletableFuture<List<T>> completableFuture = new CompletableFuture<>();
        List<UUID> uuids = globalStorage().filteredUUIDs(type, filter);
        List<T> data = new ArrayList<>();

        for (UUID uuid : uuids) {
            CompletableFuture<T> future = loadAsync(type, uuid, loadingStrategy, creationStrategies);
            future.thenAccept(t -> {
                data.add(t);
            });
        }

        completableFuture.complete(data);
        return completableFuture;
    }

    @NotNull <T extends PipelineData> Set<T> load(@NotNull Class<? extends T> type, @NotNull LoadingStrategy loadingStrategy);

    @NotNull <T extends PipelineData> CompletableFuture<Set<T>> loadAsync(@NotNull Class<? extends T> type, @NotNull LoadingStrategy loadingStrategy);

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