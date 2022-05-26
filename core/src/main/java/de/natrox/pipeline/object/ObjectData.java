/*
 * Copyright 2020-2022 NatroxMC
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

import de.natrox.common.validate.Check;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.object.annotation.AnnotationResolver;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
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

    public @NotNull DocumentData serialize() {
        DocumentData documentData = DocumentData.create();
        for (Field field : this.persistentFields()) {
            try {
                String key = AnnotationResolver.fieldName(field);
                field.setAccessible(true);
                Object value = field.get(this);

                if (value == null)
                    continue;

                documentData.append(key, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return documentData;
    }

    public void deserialize(@NotNull DocumentData document) {
        Check.notNull(document, "document");
        for (Field field : this.persistentFields()) {
            try {
                String key = AnnotationResolver.fieldName(field);
                Object value = document.get(key, field.getType());

                if (value == null)
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

        return fields
            .stream()
            .filter(field -> !Modifier.isTransient(field.getModifiers()))
            .collect(Collectors.toSet());
    }

    /**
     * Executed after instantiation of the Object
     * Executed before Object is put into the repository
     */
    public void handleCreate() {

    }

    /**
     * Executed after an Updater updated the object
     *
     * @param before The DocumentData the object had before syncing
     */
    public void handleUpdate(@NotNull DocumentData before) {

    }

    /**
     * Executed before the object is deleted from the repository.
     */
    public void handleDelete() {

    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof ObjectData pipelineData))
            return false;

        return Objects.equals(this.uniqueId(), pipelineData.uniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uniqueId());
    }
}
