package de.notion.pipeline.datatype.instance;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.datatype.instance.InstanceCreator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class DefaultInstanceCreator<T extends PipelineData> implements InstanceCreator<T> {

    @NotNull
    @Override
    public T get(Class dataClass, Pipeline pipeline) {
        try {
            Constructor<T> constructor = dataClass.getConstructor(Pipeline.class);
            constructor.setAccessible(true);

            return constructor.newInstance(pipeline);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Error while instantiating instance of class " + dataClass.getSimpleName(), e);
        }
    }
}
