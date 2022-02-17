package de.notion.pipeline.datatype;

import de.notion.pipeline.Pipeline;
import org.jetbrains.annotations.NotNull;

public abstract class ConnectionPipelineData extends PipelineData {

    public ConnectionPipelineData(@NotNull Pipeline pipeline) {
        super(pipeline);
    }

    public void onConnect() {

    }

    public void onDisconnect() {

    }
}
