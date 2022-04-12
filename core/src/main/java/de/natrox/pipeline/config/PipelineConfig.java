package de.natrox.pipeline.config;

import de.natrox.pipeline.config.part.PartConfig;
import de.natrox.pipeline.part.cache.GlobalCacheProvider;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import de.natrox.pipeline.part.updater.DataUpdaterProvider;

public final class PipelineConfig {

    private PartConfig<DataUpdaterProvider> dataUpdater;
    private PartConfig<GlobalCacheProvider> globalCache;
    private PartConfig<GlobalStorageProvider> globalStorage;

    public PartConfig<DataUpdaterProvider> dataUpdater() {
        return this.dataUpdater;
    }

    public PartConfig<GlobalCacheProvider> globalCache() {
        return this.globalCache;
    }

    public PartConfig<GlobalStorageProvider> globalStorage() {
        return this.globalStorage;
    }
}
