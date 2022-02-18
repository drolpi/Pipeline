package de.notion.pipeline.part.local.updater;

import de.notion.pipeline.datatype.PipelineData;

public interface DataUpdater {

    /**
     * Pushes the local data to Pipeline
     */
    void pushUpdate(PipelineData pipelineData, Runnable callback);

    /**
     * Notifies other Servers that hold this data to delete it from local Cache
     */
    void pushRemoval(PipelineData pipelineData, Runnable callback);
}
