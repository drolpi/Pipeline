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

package de.natrox.pipeline.part;

import de.natrox.common.container.Pair;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.stream.PipeStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StoreMap {

    byte @Nullable [] get(@NotNull UUID uniqueId);

    void put(@NotNull UUID uniqueId, byte @NotNull [] data);

    boolean contains(@NotNull UUID uniqueId);

    @NotNull Collection<UUID> keys();

    @NotNull Collection<byte[]> values();

    @NotNull Map<UUID, byte[]> entries();

    void remove(@NotNull UUID uniqueId);

    void clear();

    long size();

}
