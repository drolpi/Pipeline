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

package de.natrox.pipeline.option;

import de.natrox.pipeline.object.InstanceCreator;
import de.natrox.pipeline.object.ObjectData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public sealed interface ObjectOptions<T extends ObjectData> extends DocumentOptions, Options permits ObjectOptionsImpl {

    static <T extends ObjectData> @NotNull Builder<T> of(Class<? extends T> type) {
        return new ObjectOptionsImpl.BuilderImpl<>();
    }

    @Nullable InstanceCreator<T> instanceCreator();

    @ApiStatus.Experimental
    interface Builder<T extends ObjectData> extends OptionsBuilder<ObjectOptions<T>, Builder<T>> {

        @NotNull Builder<T> instanceCreator(@NotNull InstanceCreator<T> instanceCreator);

    }

}
