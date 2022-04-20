package de.natrox.pipeline;

import com.google.common.base.Preconditions;
import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.json.JsonProvider;
import de.natrox.pipeline.part.cache.GlobalCacheProvider;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import de.natrox.pipeline.part.updater.DataUpdaterProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class PipelineBuilder implements Pipeline.Builder {

    private PipelineRegistry pipelineRegistry;
    private DataUpdaterProvider dataUpdaterConnection;
    private GlobalCacheProvider globalCacheConnection;
    private GlobalStorageProvider globalStorageConnection;
    private JsonProvider jsonProvider;

    protected PipelineBuilder() {

    }

    @Override
    public Pipeline.@NotNull Builder registry(@NotNull PipelineRegistry registry) {
        Preconditions.checkNotNull(registry, "registry");
        this.pipelineRegistry = registry;
        return this;
    }

    @Override
    public Pipeline.@NotNull Builder jsonProvider(@NotNull JsonProvider jsonProvider) {
        Preconditions.checkNotNull(jsonProvider, "jsonProvider");
        this.jsonProvider = jsonProvider;
        return this;
    }

    @Override
    public Pipeline.@NotNull Builder dataUpdater(@Nullable DataUpdaterProvider connection) {
        this.dataUpdaterConnection = connection;
        return this;
    }

    @Override
    public Pipeline.@NotNull Builder globalCache(@Nullable GlobalCacheProvider connection) {
        this.globalCacheConnection = connection;
        return this;
    }

    @Override
    public Pipeline.@NotNull Builder globalStorage(@Nullable GlobalStorageProvider connection) {
        this.globalStorageConnection = connection;
        return this;
    }

    @Override
    public @NotNull Pipeline build() {
        return new PipelineImpl(
            dataUpdaterConnection,
            globalCacheConnection,
            globalStorageConnection,
            jsonProvider,
            pipelineRegistry
        );
    }
}
