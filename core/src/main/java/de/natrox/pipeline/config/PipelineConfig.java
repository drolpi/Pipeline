package de.natrox.pipeline.config;

import de.natrox.pipeline.config.part.PartConfig;
import de.natrox.pipeline.part.cache.GlobalCacheProvider;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import de.natrox.pipeline.part.updater.DataUpdaterProvider;
import org.jetbrains.annotations.Nullable;

public final class PipelineConfig {

    //TODO: Implement config loader

    private PartConfig<DataUpdaterProvider> dataUpdater;
    private PartConfig<GlobalCacheProvider> globalCache;
    private PartConfig<GlobalStorageProvider> globalStorage;

    public @Nullable PartConfig<DataUpdaterProvider> dataUpdater() {
        return this.dataUpdater;
    }

    public @Nullable PartConfig<GlobalCacheProvider> globalCache() {
        return this.globalCache;
    }

    public @Nullable PartConfig<GlobalStorageProvider> globalStorage() {
        return this.globalStorage;
    }
}
