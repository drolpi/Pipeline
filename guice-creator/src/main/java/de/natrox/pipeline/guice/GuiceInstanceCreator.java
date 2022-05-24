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

package de.natrox.pipeline.guice;

import com.google.inject.Injector;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.object.InstanceCreator;
import de.natrox.pipeline.object.ObjectData;
import org.jetbrains.annotations.NotNull;

public sealed interface GuiceInstanceCreator<T extends ObjectData> extends InstanceCreator<T> permits GuiceInstanceCreatorImpl {

    static <T extends ObjectData> @NotNull GuiceInstanceCreator<T> create(@NotNull Class<? extends T> type, @NotNull Injector injector) {
        Check.notNull(type, "type");
        Check.notNull(injector, "injector");
        return new GuiceInstanceCreatorImpl<>(type, injector);
    }
}
