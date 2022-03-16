package de.notion.pipeline.part.updater;

import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DefaultDataUpdater extends AbstractDataUpdater {

    @Override
    public void pushUpdate(@NotNull PipelineData pipelineData, @Nullable Runnable callback) {
        if (callback != null)
            callback.run();
    }

    @Override
    public void pushRemoval(@NotNull PipelineData pipelineData, @Nullable Runnable callback) {
        if (callback != null)
            callback.run();
    }

}
