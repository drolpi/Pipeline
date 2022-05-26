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

package de.natrox.pipeline.bin;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.mapper.DocumentMapper;
import de.natrox.pipeline.part.AbstractStore;
import de.natrox.pipeline.part.StoreMap;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class BinStore extends AbstractStore {

    private final Path directory;
    private final DocumentMapper documentMapper;

    BinStore(Pipeline pipeline, BinConfig binConfig) {
        this.documentMapper = pipeline.documentMapper();
        this.directory = Path.of(binConfig.directory());
    }

    @Override
    protected StoreMap createMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        return new BinMap(mapName, this.directory, this.documentMapper);
    }

    @Override
    public @NotNull Set<String> maps() {
        try (Stream<Path> stream = Files.walk(this.directory, 1)) {
            return stream
                .skip(1)
                .map(path -> path.toFile().getName())
                .collect(Collectors.toSet());
        } catch (IOException e) {
            return Set.of();
        }
    }

    @Override
    public boolean hasMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        return Files.exists(this.directory.resolve(mapName));
    }

    @Override
    public void removeMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        try {
            Files.delete(this.directory.resolve(mapName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.storeMapRegistry.remove(mapName);
    }
}
