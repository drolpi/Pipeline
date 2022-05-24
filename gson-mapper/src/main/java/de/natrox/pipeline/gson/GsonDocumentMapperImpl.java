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

package de.natrox.pipeline.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import de.natrox.pipeline.document.DocumentData;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.Writer;

final class GsonDocumentMapperImpl implements GsonDocumentMapper {

    public final Gson gson = new GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
        .registerTypeAdapter(DocumentData.class, new DocumentDataDeserializer())
        .create();

    GsonDocumentMapperImpl() {

    }

    @Override
    public @NotNull String writeAsString(@NotNull DocumentData documentData) {
        return this.gson.toJson(documentData);
    }

    @Override
    public void write(@NotNull Writer writer, @NotNull DocumentData documentData) {
        this.gson.toJson(documentData, writer);
    }

    @Override
    public @NotNull DocumentData read(@NotNull String json) {
        return this.gson.fromJson(json, DocumentData.class);
    }

    @Override
    public @NotNull DocumentData read(@NotNull Reader reader) {
        return this.gson.fromJson(JsonParser.parseReader(reader), DocumentData.class);
    }
}
