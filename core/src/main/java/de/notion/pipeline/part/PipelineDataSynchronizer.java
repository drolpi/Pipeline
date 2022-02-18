package de.notion.pipeline.part;

import de.notion.common.system.SystemLoadable;
import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PipelineDataSynchronizer extends SystemLoadable {

    CompletableFuture<Boolean> synchronize(@NotNull DataSourceType source, @NotNull DataSourceType destination, @NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID);

    CompletableFuture<Boolean> synchronize(@NotNull DataSourceType source, @NotNull DataSourceType destination, @NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, Runnable callback);

    enum DataSourceType {
        LOCAL,
        GLOBAL_CACHE,
        GLOBAL_STORAGE
    }
}
