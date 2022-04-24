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

package de.natrox.pipeline.part.map;

import de.natrox.common.container.Pair;
import de.natrox.pipeline.document.PipeDocument;
import de.natrox.pipeline.stream.PipeStream;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

final class EmptyPartMap implements PartMap {

    EmptyPartMap() {

    }

    @Override
    public PipeDocument get(@NotNull UUID uniqueId) {
        return null;
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull PipeDocument document) {

    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        return false;
    }

    @Override
    public @NotNull PipeStream<UUID> keys() {
        return null;
    }

    @Override
    public @NotNull PipeStream<PipeDocument> values() {
        return null;
    }

    @Override
    public @NotNull PipeStream<Pair<UUID, PipeDocument>> entries() {
        return null;
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {

    }

    @Override
    public void clear() {

    }

    @Override
    public long size() {
        return 0;
    }
}
