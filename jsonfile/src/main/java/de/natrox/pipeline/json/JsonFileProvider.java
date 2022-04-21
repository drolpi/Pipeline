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

package de.natrox.pipeline.json;

import de.natrox.common.validate.Check;
import de.natrox.common.io.FileUtil;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public final class JsonFileProvider implements GlobalStorageProvider {

    private final Path path;

    protected JsonFileProvider(@NotNull JsonFileConfig config) throws Exception {
        Check.notNull(config, "config");
        this.path = Path.of(config.path());
        var parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            FileUtil.createDirectory(parent);
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new JsonFileStorage(pipeline, path);
    }

}
