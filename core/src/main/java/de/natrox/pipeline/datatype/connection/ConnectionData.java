package de.natrox.pipeline.datatype.connection;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

public abstract class ConnectionData extends PipelineData {

    public ConnectionData(@NotNull Pipeline pipeline) {
        super(pipeline);
    }

    public void onConnect() {
    }

    public void onDisconnect() {
    }
}
