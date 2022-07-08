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

package de.natrox.pipeline.serialize;

import de.natrox.pipeline.document.DocumentData;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStream;

public sealed interface DocumentSerializer permits DocumentSerializerImpl {

    static @NotNull DocumentSerializer create() {
        return new DocumentSerializerImpl();
    }

    void write(@NotNull OutputStream outputStream, @NotNull DocumentData data);

    byte @NotNull [] write(@NotNull DocumentData data);

    @NotNull DocumentData read(@NotNull InputStream inputStream);

    @NotNull DocumentData read(byte @NotNull [] bytes);

}
