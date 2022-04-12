package de.natrox.pipeline;

import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.part.cache.GlobalCacheProvider;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import de.natrox.pipeline.part.updater.DataUpdaterProvider;
import org.jetbrains.annotations.NotNull;

final class PipelineBuilder implements Pipeline.Builder {

    private PipelineRegistry pipelineRegistry;
    private DataUpdaterProvider dataUpdaterConnection;
    private GlobalCacheProvider globalCacheConnection;
    private GlobalStorageProvider globalStorageConnection;

    protected PipelineBuilder() {

    }

    @Override
    public Pipeline.@NotNull Builder registry(@NotNull PipelineRegistry registry) {
        this.pipelineRegistry = registry;
        return this;
    }

    @Override
    public Pipeline.@NotNull Builder dataUpdater(DataUpdaterProvider connection) {
        this.dataUpdaterConnection = connection;
        return this;
    }

    @Override
    public Pipeline.@NotNull Builder globalCache(GlobalCacheProvider connection) {
        this.globalCacheConnection = connection;
        return this;
    }

    @Override
    public Pipeline.@NotNull Builder globalStorage(GlobalStorageProvider connection) {
        this.globalStorageConnection = connection;
        return this;
    }

    @Override
    public @NotNull Pipeline build() throws Exception {
        return new PipelineImpl(dataUpdaterConnection, globalCacheConnection, globalStorageConnection, pipelineRegistry);
    }
}
