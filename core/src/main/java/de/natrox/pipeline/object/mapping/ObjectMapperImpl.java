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

package de.natrox.pipeline.object.mapping;

import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.object.InstanceCreator;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.object.annotation.AnnotationResolver;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("ClassCanBeRecord")
final class ObjectMapperImpl<T extends ObjectData> implements ObjectMapper<T> {

    private final Class<? super T> type;

    ObjectMapperImpl(Class<? super T> type) {
        this.type = type;
    }

    @Override
    public void load(@NotNull T objectData, @NotNull DocumentData documentData) {
        for (Field field : this.persistentFields()) {
            try {
                String key = AnnotationResolver.fieldName(field);
                Object value = documentData.get(key, field.getType());

                if (value == null)
                    continue;

                field.setAccessible(true);
                field.set(objectData, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public @NotNull DocumentData save(@NotNull T objectData) {
        DocumentData documentData = DocumentData.create();
        for (Field field : this.persistentFields()) {
            this.saveSingle(field, objectData, documentData);
        }
        return documentData;
    }

    private void saveSingle(Field field, T objectData, DocumentData documentData) {
        try {
            String key = AnnotationResolver.fieldName(field);
            field.setAccessible(true);
            Object value = field.get(objectData);

            if (value == null)
                return;

            documentData.append(key, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Set<Field> persistentFields() {
        Set<Field> fields = new HashSet<>();
        Class<?> type = this.type;

        while (type != null) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }

        return fields
            .stream()
            .filter(field -> !Modifier.isTransient(field.getModifiers()))
            .collect(Collectors.toSet());
    }
}
