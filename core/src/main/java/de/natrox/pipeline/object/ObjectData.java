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

package de.natrox.pipeline.object;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.document.PipeDocument;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class ObjectData {

    private final transient Pipeline pipeline;
    private UUID uniqueId;

    public ObjectData(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public @NotNull UUID uniqueId() {
        return this.uniqueId;
    }

    public PipeDocument serialize() {
        PipeDocument document = PipeDocument.create();
        for (Field field : persistentFields()) {
            try {
                String key = field.getName();
                field.setAccessible(true);
                Object value = field.get(this);

                if(value == null)
                    continue;

                document.put(key, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return document;
    }

    public void deserialize(PipeDocument document) {
        for (Field field : persistentFields()) {
            try {
                String key = field.getName();
                Object value = document.get(key);

                if(value == null)
                    continue;

                field.setAccessible(true);
                field.set(this, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Set<Field> persistentFields() {
        Set<Field> fields = new HashSet<>();
        Class<?> type = getClass();

        while (type != null) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }

        return fields.stream().filter(field -> !Modifier.isTransient(field.getModifiers())).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof ObjectData pipelineData))
            return false;

        return Objects.equals(uniqueId(), pipelineData.uniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId());
    }
}
