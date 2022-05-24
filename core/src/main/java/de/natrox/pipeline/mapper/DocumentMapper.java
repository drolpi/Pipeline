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

import de.natrox.pipeline.document.DocumentData;
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
public interface DocumentMapper {

    @NotNull String writeAsString(@NotNull DocumentData documentData);

    void write(@NotNull Writer writer, @NotNull DocumentData documentData);

    @NotNull DocumentData read(@NotNull String json);

    @NotNull DocumentData read(@NotNull Reader reader);

    default @NotNull DocumentData read(@NotNull InputStream inputStream) {
        return this.read(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    default @NotNull DocumentData read(@NotNull Path path) throws IOException {
        return this.read(Files.newInputStream(path));
    }

}
