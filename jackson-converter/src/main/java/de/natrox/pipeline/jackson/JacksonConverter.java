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

package de.natrox.pipeline.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.json.JsonConverter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public final class JacksonConverter implements JsonConverter {

    private final ObjectMapper objectMapper;

    private JacksonConverter() {
        SimpleModule simpleModule = new SimpleModule()
            .addDeserializer(DocumentData.class, new DocumentDataDeserializer());

        this.objectMapper = JsonMapper.builder()
            .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .visibility(VisibilityChecker.Std
                .defaultInstance()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE))
            .addModule(simpleModule)
            .build();
    }

    public static @NotNull JacksonConverter create() {
        return new JacksonConverter();
    }

    @Override
    public @NotNull String writeAsString(@NotNull Object object) {
        try {
            return this.objectMapper.writeValueAsString(object);
        } catch (Exception exception) {
            throw new RuntimeException("Unable to write json", exception);
        }
    }

    @Override
    public void write(@NotNull Writer writer, @NotNull Object object) {
        try {
            this.objectMapper.writeValue(writer, object);
        } catch (Exception exception) {
            throw new RuntimeException("Unable to write json", exception);
        }
    }

    @Override
    public <T> @NotNull T read(@NotNull String json, Class<? extends T> type) {
        try {
            return this.objectMapper.readValue(json, type);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public <T> @NotNull T read(@NotNull Reader reader, Class<? extends T> type) {
        try {
            return this.objectMapper.readValue(reader, type);
        } catch (Exception exception) {
            throw new RuntimeException("Unable to parse json from reader", exception);
        }
    }

    @Override
    public <T> @NotNull T convert(@NotNull Object object, Class<? extends T> type) {
        return this.objectMapper.convertValue(object, type);
    }
}
