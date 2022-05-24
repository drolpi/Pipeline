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

package de.natrox.pipeline;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.mapper.DocumentMapper;
import org.jetbrains.annotations.NotNull;

final class PipelineBuilderImpl implements Pipeline.Builder {

    private final PartBundle bundle;
    private DocumentMapper documentMapper;

    PipelineBuilderImpl(PartBundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public Pipeline.@NotNull Builder documentMapper(@NotNull DocumentMapper documentMapper) {
        Check.notNull(documentMapper, "mapper");
        this.documentMapper = documentMapper;
        return this;
    }

    @Override
    public @NotNull Pipeline build() {
        return new PipelineImpl(this.bundle, this.documentMapper);
    }
}
