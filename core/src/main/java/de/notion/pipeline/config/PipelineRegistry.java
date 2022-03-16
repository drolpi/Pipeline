package de.notion.pipeline.config;

import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.datatype.instance.InstanceCreator;
import de.notion.pipeline.datatype.instance.DefaultInstanceCreator;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class PipelineRegistry {

    private final Map<Class<? extends PipelineData>, InstanceCreator> registry;

    public PipelineRegistry() {
        this.registry = new HashMap<>();
    }

    public <T extends PipelineData> void register(@NotNull Class<T> dataClass) {
        register(dataClass, new DefaultInstanceCreator<>());
    }

    public <T extends PipelineData> void register(@NotNull Class<T> dataClass, @NotNull InstanceCreator<T> instanceCreator) {
        Objects.requireNonNull(dataClass, "Dataclass can't be null");
        Objects.requireNonNull(instanceCreator, "InstanceCreator can't be null");
        registry.put(dataClass, instanceCreator);
    }

    @NotNull
    public Set<Class<? extends PipelineData>> dataClasses() {
        return registry.keySet();
    }

    public boolean isRegistered(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "Dataclass can't be null");
        return dataClasses().contains(dataClass);
    }

    @NotNull
    public InstanceCreator instanceCreator(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "Dataclass can't be null");
        if (!isRegistered(dataClass))
            throw new IllegalStateException("The class " + dataClass.getSimpleName() + " is not registered in the pipeline");
        return registry.get(dataClass);
    }

}
