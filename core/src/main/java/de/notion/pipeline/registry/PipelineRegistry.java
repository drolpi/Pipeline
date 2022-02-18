package de.notion.pipeline.registry;

import de.notion.pipeline.datatype.PipelineData;

import java.util.HashSet;
import java.util.Set;

public class PipelineRegistry {

    private final Set<Class<? extends PipelineData>> registry;

    public PipelineRegistry() {
        this.registry = new HashSet<>();
    }

    public void register(Class<? extends PipelineData> dataClass) {
        registry.add(dataClass);
    }

    public Set<Class<? extends PipelineData>> getDataClasses() {
        return registry;
    }

}
