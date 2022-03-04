package de.notion.pipeline.datatype.instance;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface InstanceCreator<T extends PipelineData> {

    @NotNull
    T get(Class<? extends T> dataClass, Pipeline pipeline);

}
