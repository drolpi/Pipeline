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

package de.natrox.pipeline.document.option;

import de.natrox.pipeline.repository.Options;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface DocumentOptions extends Options {

    @NotNull DocumentOptions DEFAULT = DocumentOptions.builder().build();

    static @NotNull Builder builder() {
        return new DocumentOptionsBuilderImpl();
    }

    @ApiStatus.Experimental
    sealed interface Builder extends OptionsBuilder<DocumentOptions, Builder> permits DocumentOptionsBuilderImpl {

    }

}
