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

package de.natrox.pipeline.gson;

import com.google.common.base.Defaults;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.natrox.common.container.Pair;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Internal
final class RecordTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> @Nullable TypeAdapter<T> create(@NotNull Gson gson, @NotNull TypeToken<T> type) {
        if (!type.getRawType().isRecord()) {
            return null;
        }

        var delegate = gson.getDelegateAdapter(this, type);
        return new RecordTypeAdapter<>(gson, delegate, type.getRawType());
    }

    private static final class RecordTypeAdapter<T> extends TypeAdapter<T> {

        private final Gson gson;
        private final TypeAdapter<T> delegate;
        private final Class<? super T> originalType;

        private final Class<?>[] recordComponentTypes;
        private final Map<String, Pair<TypeToken<?>, Integer>> componentTypes;

        private final Constructor<?> constructor;

        public RecordTypeAdapter(
            @NotNull Gson gson,
            @NotNull TypeAdapter<T> delegate,
            @NotNull Class<? super T> originalType
        ) {
            this.gson = gson;
            this.delegate = delegate;
            this.originalType = originalType;

            var components = originalType.getRecordComponents();
            this.recordComponentTypes = new Class[components.length];
            this.componentTypes = new ConcurrentHashMap<>(components.length);
            for (int i = 0; i < components.length; i++) {
                this.recordComponentTypes[i] = components[i].getType();
                this.componentTypes.put(
                    components[i].getName(),
                    new Pair<>(TypeToken.get(components[i].getGenericType()), i));
            }

            try {
                this.constructor = originalType.getDeclaredConstructor(this.recordComponentTypes);
                this.constructor.setAccessible(true);
            } catch (NoSuchMethodException exception) {
                throw new IllegalArgumentException(String.format(
                    "Unable to resolve record constructor in %s with arguments %s",
                    originalType.getName(),
                    Arrays.toString(this.recordComponentTypes)
                ), exception);
            }
        }

        @Override
        public void write(@NotNull JsonWriter out, @Nullable T value) throws IOException {
            this.delegate.write(out, value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @Nullable T read(@NotNull JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            } else {
                var arguments = new Object[this.componentTypes.size()];

                in.beginObject();
                while (in.hasNext()) {
                    var componentInfo = this.componentTypes.get(in.nextName());
                    if (componentInfo != null) {
                        arguments[componentInfo.second()] = this.gson.getAdapter(componentInfo.first()).read(in);
                    } else {
                        in.skipValue();
                    }
                }
                in.endObject();

                for (int i = 0; i < arguments.length; i++) {
                    var argumentType = this.recordComponentTypes[i];
                    if (argumentType.isPrimitive() && arguments[i] == null) {
                        arguments[i] = Defaults.defaultValue(argumentType);
                    }
                }

                try {
                    return (T) this.constructor.newInstance(arguments);
                } catch (Throwable throwable) {
                    throw new IllegalArgumentException(String.format(
                        "Unable to create instance of %s with arguments %s",
                        this.originalType.getName(),
                        Arrays.toString(arguments)
                    ), throwable);
                }
            }
        }
    }
}
