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

package de.natrox.pipeline.json.document;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface JsonDocument extends Serializable, Readable, Persistable, Iterable<String>, Cloneable {

    @NotNull Collection<String> keys();

    int size();

    @NotNull JsonDocument clear();

    @NotNull JsonDocument remove(@NotNull String key);

    boolean contains(@NotNull String key);

    @NotNull JsonDocument append(@NotNull String key, @Nullable Object value);

    @NotNull JsonDocument append(@NotNull String key, @Nullable Number value);

    @NotNull JsonDocument append(@NotNull String key, @Nullable Boolean value);

    @NotNull JsonDocument append(@NotNull String key, @Nullable String value);

    @NotNull JsonDocument append(@NotNull String key, @Nullable Character value);

    @NotNull JsonDocument append(@NotNull String key, @Nullable JsonDocument value);

    @NotNull JsonDocument append(@Nullable JsonDocument document);

    @NotNull JsonDocument appendNull(@NotNull String key);

    @NotNull JsonDocument getDocument(@NotNull String key);

    default int getInt(@NotNull String key) {
        return this.getInt(key, 0);
    }

    default double getDouble(@NotNull String key) {
        return this.getDouble(key, 0);
    }

    default float getFloat(@NotNull String key) {
        return this.getFloat(key, 0);
    }

    default byte getByte(@NotNull String key) {
        return this.getByte(key, (byte) 0);
    }

    default short getShort(@NotNull String key) {
        return this.getShort(key, (short) 0);
    }

    default long getLong(@NotNull String key) {
        return this.getLong(key, 0);
    }

    default boolean getBoolean(@NotNull String key) {
        return this.getBoolean(key, false);
    }

    default @UnknownNullability String getString(@NotNull String key) {
        return this.getString(key, null);
    }

    default char getChar(@NotNull String key) {
        return this.getChar(key, (char) 0);
    }

    default @UnknownNullability Object get(@NotNull String key) {
        return this.get(key, (Object) null);
    }

    default @UnknownNullability <T> T get(@NotNull String key, @NotNull Class<T> clazz) {
        return this.get(key, clazz, null);
    }

    default @UnknownNullability <T> T get(@NotNull String key, @NotNull Type type) {
        return this.get(key, type, null);
    }

    @UnknownNullability JsonDocument getDocument(@NotNull String key, @Nullable JsonDocument def);

    int getInt(@NotNull String key, int def);

    double getDouble(@NotNull String key, double def);

    float getFloat(@NotNull String key, float def);

    byte getByte(@NotNull String key, byte def);

    short getShort(@NotNull String key, short def);

    long getLong(@NotNull String key, long def);

    boolean getBoolean(@NotNull String key, boolean def);

    @UnknownNullability String getString(@NotNull String key, @Nullable String def);

    char getChar(@NotNull String key, char def);

    @UnknownNullability Object get(@NotNull String key, @Nullable Object def);

    @UnknownNullability <T> T get(@NotNull String key, @NotNull Class<T> clazz, @Nullable T def);

    @UnknownNullability <T> T get(@NotNull String key, @NotNull Type type, @Nullable T def);

    default boolean empty() {
        return this.size() == 0;
    }

    default @NotNull Stream<String> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    interface Factory {

        @NotNull JsonDocument emptyDocument();

        @NotNull JsonDocument newDocument();

        @NotNull JsonDocument newDocument(@Nullable Object value);

        default @NotNull JsonDocument newDocument(@NotNull String key, @Nullable Object value) {
            // we can ignore null
            if (value == null) {
                return newDocument().append(key, (Object) null);
            }
            // append the correct type for the value
            if (value instanceof Number) {
                return newDocument().append(key, (Number) value);
            } else if (value instanceof Character) {
                return newDocument().append(key, (Character) value);
            } else if (value instanceof String) {
                return newDocument().append(key, (String) value);
            } else if (value instanceof Boolean) {
                return newDocument().append(key, (Boolean) value);
            } else if (value instanceof JsonDocument) {
                return newDocument().append(key, (JsonDocument) value);
            } else {
                return newDocument().append(key, value);
            }
        }

        default @NotNull JsonDocument fromJsonBytes(byte[] bytes) {
            return fromJsonString(new String(bytes, StandardCharsets.UTF_8));
        }

        @NotNull JsonDocument fromJsonString(@NotNull String json);

        default @NotNull JsonDocument newDocument(@NotNull InputStream stream) {
            var document = newDocument();
            document.read(stream);
            return document;
        }

        default @NotNull JsonDocument newDocument(@NotNull Path path) {
            var document = newDocument();
            document.read(path);
            return document;
        }

    }
}
