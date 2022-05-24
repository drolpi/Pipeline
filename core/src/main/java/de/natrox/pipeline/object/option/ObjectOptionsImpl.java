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

package de.natrox.pipeline.object.option;

import de.natrox.pipeline.document.option.DocumentOptions;
import de.natrox.pipeline.object.InstanceCreator;
import de.natrox.pipeline.object.ObjectData;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
final class ObjectOptionsImpl<T extends ObjectData> implements ObjectOptions<T> {

    private final InstanceCreator<T> instanceCreator;

    public ObjectOptionsImpl(InstanceCreator<T> instanceCreator) {
        this.instanceCreator = instanceCreator;
    }

    @Override
    public InstanceCreator<T> instanceCreator() {
        return this.instanceCreator;
    }

    @Override
    public @NotNull DocumentOptions toDocumentOptions() {
        //TODO: Very important
        return null;
    }
}
