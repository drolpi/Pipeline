package de.notion.pipeline.registry.instance;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.datatype.PipelineData;

@FunctionalInterface
public interface InstanceCreator<T extends PipelineData> {

    T get(Class<? extends T> dataClass, Pipeline pipeline);

}
