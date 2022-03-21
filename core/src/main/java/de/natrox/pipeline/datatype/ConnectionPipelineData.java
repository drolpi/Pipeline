package de.natrox.pipeline.datatype;

import de.natrox.pipeline.Pipeline;
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
