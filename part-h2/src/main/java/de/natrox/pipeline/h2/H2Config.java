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

package de.natrox.pipeline.h2;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.PartConfig;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class H2Config implements PartConfig<H2ProviderImpl> {

    //TODO: Find better solution ?? Maybe use Path
    String directory;

    H2Config() {

    }

    public @NotNull H2Config path(@NotNull Path directory) {
        Check.notNull(directory, "directory");
        this.directory = directory.toFile().getAbsolutePath();
        return this;
    }

    @Override
    public @NotNull H2ProviderImpl buildProvider() {
        return new H2ProviderImpl(this);
    }

}
