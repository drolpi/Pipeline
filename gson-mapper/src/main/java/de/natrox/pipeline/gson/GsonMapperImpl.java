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
import com.google.gson.JsonParser;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.mapper.Mapper;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.Writer;

final class GsonMapperImpl implements GsonMapper {

    public final Gson gson = new GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
        .registerTypeAdapter(DocumentData.class, new DocumentDataDeserializer())
        .create();

    GsonMapperImpl() {

    }

    @Override
    public @NotNull String writeAsString(@NotNull Object object) {
        return this.gson.toJson(object);
    }

    @Override
    public void write(@NotNull Writer writer, @NotNull Object object) {
        this.gson.toJson(object, writer);
    }

    @Override
    public <T> @NotNull T read(@NotNull String json, Class<? extends T> type) {
        return this.gson.fromJson(json, type);
    }

    @Override
    public <T> @NotNull T read(@NotNull Reader reader, Class<? extends T> type) {
        return this.gson.fromJson(JsonParser.parseReader(reader), type);
    }

    @Override
    public <T> @NotNull T convert(@NotNull Object object, Class<? extends T> type) {
        return this.read(this.writeAsString(object), type);
    }
}
