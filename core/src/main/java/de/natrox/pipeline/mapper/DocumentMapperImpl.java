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

package de.natrox.pipeline.mapper;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.serializers.MapSerializer;
import de.natrox.pipeline.document.DocumentData;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

final class DocumentMapperImpl implements DocumentMapper {

    private final Kryo kryo = new Kryo();

    public DocumentMapperImpl() {
        this.kryo.setRegistrationRequired(false);
        this.kryo.register(DocumentData.class, new DocumentSerializer());
        this.kryo.register(UUID.class, new UUIDSerializer());
    }

    @Override
    public void write(@NotNull OutputStream outputStream, @NotNull DocumentData data) {
        try (Output output = new Output(outputStream)) {
            synchronized (this.kryo) {
                this.kryo.writeObject(output, data);
            }
        }
    }

    @Override
    public byte @NotNull [] write(@NotNull DocumentData data) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            this.write(byteArrayOutputStream, data);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close output stream", e);
        }
    }

    @Override
    public @NotNull DocumentData read(@NotNull InputStream inputStream) {
        try (Input input = new Input(inputStream)) {
            synchronized (this.kryo) {
                return this.kryo.readObject(input, DocumentData.class);
            }
        }
    }

    @Override
    public @NotNull DocumentData read(byte @NotNull [] bytes) {
        try (Input input = new Input(bytes, 0, bytes.length)) {
            synchronized (this.kryo) {
                return this.kryo.readObject(input, DocumentData.class);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private final static class DocumentSerializer extends Serializer<DocumentData> {
        private final MapSerializer mapSerializer = new MapSerializer() {
            @Override
            protected Map create(Kryo kryo, Input input, Class type, int size) {
                return (Map) DocumentData.create();
            }
        };

        @Override
        public void write(Kryo kryo, Output output, DocumentData document) {
            this.mapSerializer.write(kryo, output, (Map) document);
        }

        @Override
        public DocumentData read(Kryo kryo, Input input, Class<? extends DocumentData> type) {
            DocumentData document = DocumentData.create();
            Map<?, ?> map = this.mapSerializer.read(kryo, input, Map.class);
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                document.append((String) entry.getKey(), entry.getValue());
            }

            return document;
        }
    }

    private final static class UUIDSerializer extends Serializer<UUID> {

        @Override
        public void write(Kryo kryo, Output output, UUID object) {
            output.writeString(object.toString());
        }

        @Override
        public UUID read(Kryo kryo, Input input, Class<? extends UUID> type) {
            return UUID.fromString(input.readString());
        }
    }
}
