package de.natrox.pipeline.datatype.instance;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface InstanceCreator<T extends PipelineData> {

    @NotNull
    T get(Class<? extends T> dataClass, Pipeline pipeline);

}
