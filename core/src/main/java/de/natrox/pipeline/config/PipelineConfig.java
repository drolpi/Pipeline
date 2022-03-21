package de.natrox.pipeline.config;

import de.natrox.pipeline.config.connection.DataUpdaterConnection;
import de.natrox.pipeline.config.connection.GlobalCacheConnection;
import de.natrox.pipeline.config.connection.GlobalStorageConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final record PipelineConfig(DataUpdaterConnection dataUpdaterConnection,
                                   GlobalCacheConnection globalCacheConnection,
                                   GlobalStorageConnection globalStorageConnection) {

    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    @Nullable
    public DataUpdaterConnection dataUpdaterConnection() {
        return dataUpdaterConnection;
    }

    @Nullable
    public GlobalCacheConnection globalCacheConnection() {
        return globalCacheConnection;
    }

    @Nullable
    public GlobalStorageConnection globalStorageConnection() {
        return globalStorageConnection;
    }

    public static class Builder {

        private DataUpdaterConnection dataUpdaterConnection;
        private GlobalCacheConnection globalCacheConnection;
        private GlobalStorageConnection globalStorageConnection;

        @NotNull
        public Builder dataUpdater(@Nullable DataUpdaterConnection dataUpdaterConnection) {
            this.dataUpdaterConnection = dataUpdaterConnection;
            return this;
        }

        @NotNull
        public Builder globalCache(@Nullable GlobalCacheConnection globalCacheConnection) {
            this.globalCacheConnection = globalCacheConnection;
            return this;
        }

        @NotNull
        public Builder globalStorage(@Nullable GlobalStorageConnection globalStorageConnection) {
            this.globalStorageConnection = globalStorageConnection;
            return this;
        }

        @NotNull
        public PipelineConfig build() {
            return new PipelineConfig(dataUpdaterConnection, globalCacheConnection, globalStorageConnection);
        }
    }
}
