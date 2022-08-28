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

package de.natrox.pipeline.serializer.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.natrox.pipeline.node.DataNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

final class GsonNodeSerializerImpl implements GsonNodeSerializer {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    GsonNodeSerializerImpl() {

    }

    @Override
    public void write(@NotNull OutputStream outputStream, @NotNull DataNode node) {
        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
            synchronized (this.gson) {
                this.gson.toJson(node, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to close output stream", e);
        }
    }

    @Override
    public @NotNull String write(@NotNull DataNode node) {
        synchronized (this.gson) {
            return this.gson.toJson(node);
        }
    }

    @Override
    public @NotNull DataNode read(@NotNull InputStream inputStream) {
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            synchronized (this.gson) {
                return this.gson.fromJson(reader, DataNode.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to close input stream", e);
        }
    }

    @Override
    public @NotNull DataNode read(@NotNull String data) {
        return this.gson.fromJson(data, DataNode.class);
    }
}
