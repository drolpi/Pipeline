package de.notion.pipeline.config;

import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.datatype.instance.InstanceCreator;
import de.notion.pipeline.datatype.instance.def.DefaultInstanceCreator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PipelineRegistry {

    private final Map<Class<? extends PipelineData>, InstanceCreator> registry;

    public PipelineRegistry() {
        this.registry = new HashMap<>();
    }

    public <T extends PipelineData> void register(Class<T> dataClass) {
        register(dataClass, new DefaultInstanceCreator<>());
    }

    public <T extends PipelineData> void register(Class<T> dataClass, InstanceCreator<T> instanceCreator) {
        registry.put(dataClass, instanceCreator);
    }

    public Set<Class<? extends PipelineData>> dataClasses() {
        return registry.keySet();
    }

    public InstanceCreator instanceCreator(Class<? extends PipelineData> dataClass) {
        return registry.get(dataClass);
    }

}
