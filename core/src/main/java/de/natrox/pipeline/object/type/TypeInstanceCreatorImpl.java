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

package de.natrox.pipeline.object.type;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.object.ObjectData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("ClassCanBeRecord")
final class TypeInstanceCreatorImpl<T extends ObjectData> implements TypeInstanceCreator<T> {

    private final Class<? extends T> type;

    TypeInstanceCreatorImpl(Class<? extends T> type) {
        this.type = type;
    }

    @Override
    public @NotNull T create(@NotNull Pipeline pipeline) {
        try {
            Constructor<? extends T> constructor = this.type.getConstructor(Pipeline.class);
            constructor.setAccessible(true);

            return constructor.newInstance(pipeline);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Error while instantiating instance of class " + this.type.getSimpleName(), e);
        }
    }

}
