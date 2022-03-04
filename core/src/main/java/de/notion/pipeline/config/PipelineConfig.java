package de.notion.pipeline.config;

import de.notion.pipeline.config.part.DataUpdaterConfig;
import de.notion.pipeline.config.part.GlobalCacheConfig;
import de.notion.pipeline.config.part.GlobalStorageConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final record PipelineConfig(DataUpdaterConfig updaterConfig, GlobalCacheConfig globalCacheConfig,
                                   GlobalStorageConfig globalStorageConfig) {

    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    @Override
    @Nullable
    public DataUpdaterConfig updaterConfig() {
        return updaterConfig;
    }

    @Override
    @Nullable
    public GlobalCacheConfig globalCacheConfig() {
        return globalCacheConfig;
    }

    @Override
    @Nullable
    public GlobalStorageConfig globalStorageConfig() {
        return globalStorageConfig;
    }

    public static class Builder {

        private DataUpdaterConfig updaterConfig;
        private GlobalCacheConfig globalCacheConfig;
        private GlobalStorageConfig globalStorageConfig;

        @NotNull
        public Builder updater(@Nullable DataUpdaterConfig updaterConfig) {
            this.updaterConfig = updaterConfig;
            return this;
        }

        @NotNull
        public Builder globalCache(@Nullable GlobalCacheConfig globalCacheConfig) {
            this.globalCacheConfig = globalCacheConfig;
            return this;
        }

        @NotNull
        public Builder globalStorage(@Nullable GlobalStorageConfig globalStorageConfig) {
            this.globalStorageConfig = globalStorageConfig;
            return this;
        }

        @NotNull
        public PipelineConfig build() {
            return new PipelineConfig(updaterConfig, globalCacheConfig, globalStorageConfig);
        }
    }
}
