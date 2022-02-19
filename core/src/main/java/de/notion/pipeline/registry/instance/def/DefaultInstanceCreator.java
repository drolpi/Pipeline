package de.notion.pipeline.registry.instance.def;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.registry.instance.InstanceCreator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DefaultInstanceCreator<T extends PipelineData> implements InstanceCreator<T> {

    @Override
    public PipelineData get(Class dataClass, Pipeline pipeline) {
        try {
            Constructor<T> constructor = dataClass.getConstructor(Pipeline.class);
            constructor.setAccessible(true);

            return constructor.newInstance(pipeline);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Error while instantiating instance of class " + dataClass.getSimpleName(), e);
        }
    }
}
