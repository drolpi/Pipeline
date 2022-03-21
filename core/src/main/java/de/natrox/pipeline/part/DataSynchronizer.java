package de.natrox.pipeline.part;

import de.natrox.common.Loadable;
import de.natrox.common.Shutdownable;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DataSynchronizer extends Loadable, Shutdownable {

    @NotNull <T extends PipelineData> CompletableFuture<Boolean> synchronize(
        @NotNull DataSourceType source,
        @NotNull DataSourceType destination,
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID objectUUID,
        @Nullable Runnable callback,
        @Nullable InstanceCreator<T> instanceCreator
    );

    default <T extends PipelineData> @NotNull CompletableFuture<Boolean> synchronize(
        @NotNull DataSourceType source,
        @NotNull DataSourceType destination,
        @NotNull Class<? extends PipelineData> dataClass,
        @NotNull UUID objectUUID
    ) {
        return synchronize(source, destination, dataClass, objectUUID, null, null);
    }

    enum DataSourceType {
        LOCAL,
        GLOBAL_CACHE,
        GLOBAL_STORAGE
    }
}
