package de.notion.pipeline.part.local.updater.def;

import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.local.updater.DataUpdater;
import de.notion.pipeline.part.local.updater.LoadingTaskManager;

public class DefaultDataUpdater implements DataUpdater {

    private final LoadingTaskManager loadingTaskManager;

    public DefaultDataUpdater() {
        this.loadingTaskManager = new LoadingTaskManager();
    }

    @Override
    public void pushUpdate(PipelineData pipelineData, Runnable callback) {
        callback.run();
    }

    @Override
    public void pushRemoval(PipelineData pipelineData, Runnable callback) {
        callback.run();
    }

    @Override
    public LoadingTaskManager loadingTaskManager() {
        return loadingTaskManager;
    }
}
