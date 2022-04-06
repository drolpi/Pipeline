package de.natrox.pipeline.config;

import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.DefaultInstanceCreator;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class PipelineRegistry {

    private final Map<Class<? extends PipelineData>, Registrar> registry;

    public PipelineRegistry() {
        this.registry = new HashMap<>();
    }

    public <T extends PipelineData> void register(@NotNull Class<T> dataClass) {
        register(dataClass, new DefaultInstanceCreator<>());
    }

    public <T extends PipelineData> void register(@NotNull Class<T> dataClass, @NotNull InstanceCreator<T> instanceCreator) {
        Objects.requireNonNull(dataClass, "Dataclass can't be null");
        Objects.requireNonNull(instanceCreator, "InstanceCreator can't be null");
        var identifier = AnnotationResolver.storageIdentifier(dataClass);
        if (isRegistered(dataClass) && isRegistered(identifier))
            throw new IllegalStateException("The class " + dataClass.getSimpleName() + " is already registered in the pipeline");

        registry.put(dataClass, new Registrar(instanceCreator, identifier));
    }

    @NotNull
    public Set<Class<? extends PipelineData>> dataClasses() {
        return registry.keySet();
    }

    public boolean isRegistered(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "Dataclass can't be null");
        return this.dataClasses().contains(dataClass);
    }

    public boolean isRegistered(@NotNull String identifier) {
        Objects.requireNonNull(identifier, "Identifier can't be null");
        return this.registry.values().stream().anyMatch(registrar -> registrar.identifier().equals(identifier));
    }

    @NotNull
    public InstanceCreator instanceCreator(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "Dataclass can't be null");
        if (!isRegistered(dataClass))
            throw new IllegalStateException("The class " + dataClass.getSimpleName() + " is not registered in the pipeline");
        return registry.get(dataClass).instanceCreator();
    }

    @NotNull
    public String identifier(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "Dataclass can't be null");
        if (!isRegistered(dataClass))
            throw new IllegalStateException("The class " + dataClass.getSimpleName() + " is not registered in the pipeline");
        return registry.get(dataClass).identifier();
    }

    @NotNull
    public Class<? extends PipelineData> dataClass(@NotNull String identifier) {
        Objects.requireNonNull(identifier, "Identifier can't be null");
        if (!isRegistered(identifier))
            throw new IllegalStateException("A class with the identifier " + identifier + " is not registered in the pipeline");
        return registry
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().identifier().equals(identifier))
            .findFirst()
            .map(Map.Entry::getKey)
            .orElseThrow();
    }

    record Registrar(@NotNull InstanceCreator instanceCreator, String identifier) {

    }

}
