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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.json.document.Persistable;
import de.natrox.pipeline.json.document.Readable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

final class GsonDocument implements JsonDocument {

    public static final Gson GSON = new GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
        .registerTypeHierarchyAdapter(GsonDocument.class, new JsonDocumentTypeAdapter())
        .create();

    final JsonObject object;

    public GsonDocument() {
        this(new JsonObject());
    }

    protected GsonDocument(@NotNull JsonObject object) {
        this.object = object;
    }

    @Override
    public @NotNull Collection<String> keys() {
        return this.object.keySet();
    }

    @Override
    public int size() {
        return this.object.size();
    }

    @Override
    public @NotNull GsonDocument clear() {
        for (var key : Set.copyOf(this.object.keySet())) {
            this.object.remove(key);
        }

        return this;
    }

    @Override
    public @NotNull GsonDocument remove(@NotNull String key) {
        this.object.remove(key);
        return this;
    }

    @Override
    public boolean contains(@NotNull String key) {
        return this.object.has(key);
    }

    @Override
    public @NotNull GsonDocument append(@NotNull String key, @Nullable Object value) {
        this.object.add(key, value == null ? JsonNull.INSTANCE : GSON.toJsonTree(value));
        return this;
    }

    @Override
    public @NotNull GsonDocument append(@NotNull String key, @Nullable Number value) {
        this.object.addProperty(key, value);
        return this;
    }

    @Override
    public @NotNull GsonDocument append(@NotNull String key, @Nullable Boolean value) {
        this.object.addProperty(key, value);
        return this;
    }

    @Override
    public @NotNull GsonDocument append(@NotNull String key, @Nullable String value) {
        this.object.addProperty(key, value);
        return this;
    }

    @Override
    public @NotNull GsonDocument append(@NotNull String key, @Nullable Character value) {
        this.object.addProperty(key, value);
        return this;
    }

    @Override
    public @NotNull GsonDocument append(@NotNull String key, @Nullable JsonDocument value) {
        if (value instanceof GsonDocument gsonDocument)
            this.object.add(key, gsonDocument.object);
        return this;
    }

    @Override
    public @NotNull GsonDocument append(@Nullable JsonDocument document) {
        if (document instanceof GsonDocument gsonDocument)
            for (var entry : gsonDocument.object.entrySet())
                this.object.add(entry.getKey(), entry.getValue());

        return this;
    }

    @Override
    public @NotNull GsonDocument appendNull(@NotNull String key) {
        this.object.add(key, JsonNull.INSTANCE);
        return this;
    }

    @Override
    public @NotNull GsonDocument getDocument(@NotNull String key) {
        return this.getDocument(key);
    }

    @Override
    public int getInt(@NotNull String key, int def) {
        var element = this.object.get(key);
        return element == null || !element.isJsonPrimitive() ? def : element.getAsInt();
    }

    @Override
    public double getDouble(@NotNull String key, double def) {
        var element = this.object.get(key);
        return element == null || !element.isJsonPrimitive() ? def : element.getAsDouble();
    }

    @Override
    public float getFloat(@NotNull String key, float def) {
        var element = this.object.get(key);
        return element == null || !element.isJsonPrimitive() ? def : element.getAsFloat();
    }

    @Override
    public byte getByte(@NotNull String key, byte def) {
        var element = this.object.get(key);
        return element == null || !element.isJsonPrimitive() ? def : element.getAsByte();
    }

    @Override
    public short getShort(@NotNull String key, short def) {
        var element = this.object.get(key);
        return element == null || !element.isJsonPrimitive() ? def : element.getAsShort();
    }

    @Override
    public long getLong(@NotNull String key, long def) {
        var element = this.object.get(key);
        return element == null || !element.isJsonPrimitive() ? def : element.getAsLong();
    }

    @Override
    public boolean getBoolean(@NotNull String key, boolean def) {
        var element = this.object.get(key);
        return element == null || !element.isJsonPrimitive() ? def : element.getAsBoolean();
    }

    @Override
    public @UnknownNullability String getString(@NotNull String key, @Nullable String def) {
        var element = this.object.get(key);
        return element == null || !element.isJsonPrimitive() ? def : element.getAsString();
    }

    @Override
    public char getChar(@NotNull String key, char def) {
        var fullString = this.getString(key);
        return fullString != null && fullString.length() > 0 ? fullString.charAt(0) : def;
    }

    @Override
    public @UnknownNullability Object get(@NotNull String key, @Nullable Object def) {
        var element = this.object.get(key);
        return element != null ? element : def;
    }

    @Override
    public <T> @UnknownNullability T get(@NotNull String key, @NotNull Class<T> clazz, @Nullable T def) {
        var element = this.object.get(key);
        return element == null || element.isJsonNull() ? def : GSON.fromJson(element, clazz);
    }

    @Override
    public <T> @UnknownNullability T get(@NotNull String key, @NotNull Type type, @Nullable T def) {
        var element = this.object.get(key);
        return element == null || element.isJsonNull() ? def : GSON.fromJson(element, type);
    }

    @Override
    public @UnknownNullability JsonDocument getDocument(@NotNull String key, @Nullable JsonDocument def) {
        var element = this.object.get(key);
        if (element != null && element.isJsonObject()) {
            return new GsonDocument(element.getAsJsonObject());
        } else {
            return def;
        }
    }

    @Override
    public @NotNull Persistable write(@NotNull Writer writer) {
        GSON.toJson(this.object, writer);
        return this;
    }

    @Override
    public @NotNull Readable read(@NotNull Reader reader) {
        try {
            // parse the object
            var element = JsonParser.parseReader(reader);
            if (element.isJsonObject()) {
                for (var entry : element.getAsJsonObject().entrySet()) {
                    this.object.add(entry.getKey(), entry.getValue());
                }
                return this;
            }
            // not a json object - unable to parse
            throw new JsonSyntaxException("Json element parsed from reader is not a json object");
        } catch (Exception exception) {
            throw new RuntimeException("Unable to parse json document from reader", exception);
        }
    }

    @Override
    public @NotNull Iterator<String> iterator() {
        return this.object.keySet().iterator();
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public @NotNull GsonDocument clone() {
        return new GsonDocument(this.object.deepCopy());
    }

    public @NotNull String toPrettyJson() {
        return GSON.toJson(this.object);
    }

    @Override
    public @NotNull String toString() {
        return this.object.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GsonDocument document)) {
            return false;
        }

        return document.object.equals(this.object);
    }

    @Override
    public int hashCode() {
        return this.object.hashCode();
    }
}
