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

import com.google.common.collect.ImmutableMap;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.object.InstanceCreator;
import de.natrox.pipeline.object.ObjectData;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("rawtypes")
final class ObjectOptionsBuilderImpl implements ObjectOptions.Builder {

    private final ImmutableMap.Builder<Class, InstanceCreator> instanceCreators = new ImmutableMap.Builder<>();

    @Override
    public <T extends ObjectData> ObjectOptions.@NotNull Builder instanceCreator(@NotNull Class<? extends T> type, @NotNull InstanceCreator<T> instanceCreator) {
        Check.notNull(type, "type");
        Check.notNull(instanceCreator, "instanceCreator");
        this.instanceCreators.put(type, instanceCreator);
        return this;
    }

    @Override
    public ObjectOptions build() {
        return new ObjectOptionsImpl(this.instanceCreators.build());
    }
}
