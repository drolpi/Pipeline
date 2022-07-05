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

package de.natrox.pipeline.object.annotation;

import de.natrox.common.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

@ApiStatus.Internal
public final class AnnotationResolver {

    private AnnotationResolver() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Properties properties(@NotNull Class<?> type) {
        Check.notNull(type, "type");
        Properties properties = type.getAnnotation(Properties.class);
        if (properties == null)
            throw new RuntimeException(type.getName() + " does not have Properties annotation set");
        return properties;
    }

    public static @NotNull String identifier(@NotNull Class<?> type) {
        Check.notNull(type, "type");
        return AnnotationResolver.properties(type).identifier();
    }

    public static @NotNull String fieldName(@NotNull Field field) {
        Check.notNull(field, "field");
        Named named = field.getAnnotation(Named.class);
        return named != null ? named.name() : field.getName();
    }
}
