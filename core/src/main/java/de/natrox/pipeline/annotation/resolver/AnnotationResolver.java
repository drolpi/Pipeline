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

package de.natrox.pipeline.annotation.resolver;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.annotation.AutoSave;
import de.natrox.pipeline.annotation.CleanUp;
import de.natrox.pipeline.annotation.Preload;
import de.natrox.pipeline.annotation.Properties;
import de.natrox.pipeline.annotation.property.Context;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class AnnotationResolver {

    private AnnotationResolver() {
        throw new UnsupportedOperationException();
    }

    public static Properties properties(Class<?> classType) {
        Check.notNull(classType, "classType");
        var properties = classType.getAnnotation(Properties.class);
        if (properties == null)
            throw new RuntimeException(classType.getName() + " does not have @Properties Annotation set");
        return properties;
    }

    public static @NotNull String storageIdentifier(Class<?> classType) {
        Check.notNull(classType, "classType");
        return properties(classType).identifier();
    }

    public static @NotNull Context context(Class<?> classType) {
        Check.notNull(classType, "classType");
        return properties(classType).context();
    }

    public static @NotNull Optional<Preload> preload(Class<?> classType) {
        Check.notNull(classType, "classType");
        return Optional.ofNullable(classType.getAnnotation(Preload.class));
    }

    public static @NotNull Optional<AutoSave> autoSave(Class<?> classType) {
        Check.notNull(classType, "classType");
        return Optional.ofNullable(classType.getAnnotation(AutoSave.class));
    }

    public static @NotNull Optional<CleanUp> cleanUp(Class<?> classType) {
        Check.notNull(classType, "classType");
        return Optional.ofNullable(classType.getAnnotation(CleanUp.class));
    }
}
