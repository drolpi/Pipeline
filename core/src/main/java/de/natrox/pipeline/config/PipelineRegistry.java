/*
 * Copyright 2020-2022 NatroxMC team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.natrox.pipeline.config;

import com.google.common.base.Preconditions;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.DefaultInstanceCreator;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class PipelineRegistry {

    private final Map<Class<? extends PipelineData>, Entry> registry;

    public PipelineRegistry() {
        this.registry = new HashMap<>();
    }

    public <T extends PipelineData> void register(@NotNull Class<T> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        register(dataClass, new DefaultInstanceCreator<>());
    }

    public <T extends PipelineData> void register(@NotNull Class<T> dataClass, @NotNull InstanceCreator<T> instanceCreator) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(instanceCreator, "instanceCreator");

        var identifier = AnnotationResolver.storageIdentifier(dataClass);
        if (isRegistered(dataClass) && isRegistered(identifier))
            throw new IllegalStateException("The class " + dataClass.getSimpleName() + " is already registered in the pipeline");

        registry.put(dataClass, new Entry(instanceCreator, identifier));
    }

    public @NotNull Set<Class<? extends PipelineData>> dataClasses() {
        return registry.keySet();
    }

    public boolean isRegistered(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        return this.dataClasses().contains(dataClass);
    }

    public boolean isRegistered(@NotNull String identifier) {
        Preconditions.checkNotNull(identifier, "identifier");
        return this.registry.values().stream().anyMatch(entry -> entry.identifier().equals(identifier));
    }

    public @NotNull InstanceCreator instanceCreator(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        this.checkRegistered(dataClass);

        return registry.get(dataClass).instanceCreator();
    }

    public @NotNull String identifier(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        this.checkRegistered(dataClass);

        return registry.get(dataClass).identifier();
    }

    public @NotNull Class<? extends PipelineData> dataClass(@NotNull String identifier) {
        Preconditions.checkNotNull(identifier, "identifier");
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

    public void checkRegistered(@NotNull Class<? extends PipelineData> dataClass) {
        if (isRegistered(dataClass))
            return;
        throw new IllegalStateException("The class " + dataClass.getSimpleName() + " is not registered in the pipeline");
    }

    record Entry(@NotNull InstanceCreator instanceCreator, @NotNull String identifier) {

    }

}
