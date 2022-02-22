package de.notion.pipeline.part.local.updater.def;

import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.local.updater.AbstractDataUpdater;
import de.notion.pipeline.part.local.updater.DataUpdater;

public class DefaultDataUpdater extends AbstractDataUpdater implements DataUpdater {

    @Override
    public void pushUpdate(PipelineData pipelineData, Runnable callback) {
        callback.run();
    }

    @Override
    public void pushRemoval(PipelineData pipelineData, Runnable callback) {
        callback.run();
    }
}
