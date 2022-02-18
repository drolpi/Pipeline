package de.notion.pipeline.config;

import de.notion.pipeline.config.part.GlobalCacheConfig;
import de.notion.pipeline.config.part.GlobalStorageConfig;
import de.notion.pipeline.config.part.DataUpdaterConfig;
import org.jetbrains.annotations.Nullable;

public record PipelineConfig(DataUpdaterConfig updaterConfig, GlobalCacheConfig globalCacheConfig, GlobalStorageConfig globalStorageConfig) {

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

        public Builder updater(DataUpdaterConfig updaterConfig) {
            this.updaterConfig = updaterConfig;
            return this;
        }

        public Builder globalCache(GlobalCacheConfig globalCacheConfig) {
            this.globalCacheConfig = globalCacheConfig;
            return this;
        }

        public Builder globalStorage(GlobalStorageConfig globalStorageConfig) {
            this.globalStorageConfig = globalStorageConfig;
            return this;
        }

        public PipelineConfig build() {
            return new PipelineConfig(updaterConfig, globalCacheConfig, globalStorageConfig);
        }
    }
}
