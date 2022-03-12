package de.notion.pipeline.part.local.updater.def;

import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.local.updater.DataUpdater;
import de.notion.pipeline.part.local.updater.LoadingTaskSynchronizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultDataUpdater implements DataUpdater {

    private final LoadingTaskSynchronizer loadingTaskSynchronizer;

    public DefaultDataUpdater() {
        this.loadingTaskSynchronizer = new LoadingTaskSynchronizer();
    }

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

    @NotNull
    @Override
    public LoadingTaskSynchronizer loadingTaskManager() {
        return loadingTaskSynchronizer;
    }
}
