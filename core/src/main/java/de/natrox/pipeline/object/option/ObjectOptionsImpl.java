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

package de.natrox.pipeline.object.option;

import de.natrox.pipeline.document.option.DocumentOptions;
import de.natrox.pipeline.object.InstanceCreator;
import de.natrox.pipeline.object.ObjectData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SuppressWarnings({"ClassCanBeRecord", "rawtypes"})
final class ObjectOptionsImpl implements ObjectOptions {

    private final Map<Class, InstanceCreator> instanceCreators;

    public ObjectOptionsImpl(Map<Class, InstanceCreator> instanceCreators) {
        this.instanceCreators = instanceCreators;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ObjectData> InstanceCreator<T> instanceCreator(@NotNull Class<? extends T> type) {
        return instanceCreators.get(type);
    }

    @Override
    public @NotNull DocumentOptions toDocumentOptions() {
        //TODO: Very important
        return null;
    }
}
