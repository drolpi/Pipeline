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

package de.natrox.pipeline.mapper;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@ApiStatus.Internal
public interface Mapper {

    @NotNull String writeAsString(@NotNull Object object);

    void write(@NotNull Writer writer, @NotNull Object object);

    <T> @NotNull T read(@NotNull String json, Class<? extends T> type);

    <T> @NotNull T read(@NotNull Reader reader, Class<? extends T> type);

    default <T> @NotNull T read(@NotNull InputStream inputStream, Class<? extends T> type) {
        return this.read(new InputStreamReader(inputStream, StandardCharsets.UTF_8), type);
    }

    default <T> @NotNull T read(@NotNull Path path, Class<? extends T> type) throws IOException {
        return this.read(Files.newInputStream(path), type);
    }

    <T> @NotNull T convert(@NotNull Object object, Class<? extends T> type);

}
