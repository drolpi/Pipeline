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

import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@Internal
final class JsonDocumentTypeAdapter extends TypeAdapter<GsonDocument> {

    @Override
    public void write(JsonWriter jsonWriter, GsonDocument document) throws IOException {
        TypeAdapters.JSON_ELEMENT.write(jsonWriter, document == null ? new JsonObject() : document.object);
    }

    @Override
    public @Nullable GsonDocument read(JsonReader jsonReader) throws IOException {
        var jsonElement = TypeAdapters.JSON_ELEMENT.read(jsonReader);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            return new GsonDocument(jsonElement.getAsJsonObject());
        } else {
            return null;
        }
    }
}
