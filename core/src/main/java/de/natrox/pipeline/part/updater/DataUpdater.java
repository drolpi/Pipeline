package de.natrox.pipeline.part.updater;

import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface DataUpdater {

    /**
     * Pushes the local data to Pipeline
     */
    void pushUpdate(@NotNull PipelineData pipelineData, @Nullable Runnable callback);

    /**
     * Notifies other Servers that hold this data to delete it from local Cache
     */
    void pushRemoval(@NotNull PipelineData pipelineData, @Nullable Runnable callback);

    @NotNull
    Optional<PipelineData> applySync(@NotNull PipelineData pipelineData);
}
