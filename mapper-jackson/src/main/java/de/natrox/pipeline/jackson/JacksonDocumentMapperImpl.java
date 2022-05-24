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
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

final class JacksonDocumentMapperImpl implements JacksonDocumentMapper {

    private final ObjectMapper objectMapper;

    JacksonDocumentMapperImpl() {
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

    @Override
    public @NotNull String writeAsString(@NotNull DocumentData documentData) {
        try {
            return this.objectMapper.writeValueAsString(documentData);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Unable to write json", exception);
        }
    }

    @Override
    public void write(@NotNull Writer writer, @NotNull DocumentData documentData) {
        try {
            this.objectMapper.writeValue(writer, documentData);
        } catch (IOException exception) {
            throw new RuntimeException("Unable to write json", exception);
        }
    }

    @Override
    public @NotNull DocumentData read(@NotNull String json) {
        try {
            return this.objectMapper.readValue(json, DocumentData.class);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public @NotNull DocumentData read(@NotNull Reader reader) {
        try {
            return this.objectMapper.readValue(reader, DocumentData.class);
        } catch (IOException exception) {
            throw new RuntimeException("Unable to parse json from reader", exception);
        }
    }
}
