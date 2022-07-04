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

import de.natrox.common.validate.Check;
import de.natrox.pipeline.object.InstanceCreator;
import de.natrox.pipeline.object.ObjectData;
import org.jetbrains.annotations.NotNull;

final class ObjectOptionsImpl<T extends ObjectData> extends DocumentOptionsImpl implements ObjectOptions<T> {

    private final InstanceCreator<T> instanceCreator;

    ObjectOptionsImpl(boolean useGlobalCache, boolean useLocalCache, InstanceCreator<T> instanceCreator) {
        super(useGlobalCache, useLocalCache);
        this.instanceCreator = instanceCreator;
    }

    @Override
    public InstanceCreator<T> instanceCreator() {
        return this.instanceCreator;
    }

    final static class BuilderImpl<T extends ObjectData> extends AbstractOptions.AbstractBuilder<ObjectOptions<T>, ObjectOptions.Builder<T>> implements ObjectOptions.Builder<T> {

        private InstanceCreator<T> instanceCreator;

        @Override
        public ObjectOptions.@NotNull Builder<T> instanceCreator(@NotNull InstanceCreator<T> instanceCreator) {
            Check.notNull(instanceCreator, "instanceCreator");
            this.instanceCreator = instanceCreator;
            return this;
        }

        @Override
        public ObjectOptions<T> build() {
            return new ObjectOptionsImpl<>(this.useGlobalCache, this.useLocalCache, this.instanceCreator);
        }
    }
}
