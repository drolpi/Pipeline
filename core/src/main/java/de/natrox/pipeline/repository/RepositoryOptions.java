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

package de.natrox.pipeline.repository;

import de.natrox.pipeline.object.InstanceCreator;
import de.natrox.pipeline.object.ObjectData;

public sealed abstract class RepositoryOptions {

    private final boolean useGlobalCache;
    private final boolean useLocalCache;

    RepositoryOptions(boolean useGlobalCache, boolean useLocalCache) {
        this.useGlobalCache = useGlobalCache;
        this.useLocalCache = useLocalCache;
    }

    public boolean useGlobalCache() {
        return this.useGlobalCache;
    }

    public boolean useLocalCache() {
        return this.useLocalCache;
    }

    static non-sealed class DocumentOptions extends RepositoryOptions {

        DocumentOptions(boolean useGlobalCache, boolean useLocalCache) {
            super(useGlobalCache, useLocalCache);
        }
    }

    static final class ObjectOptions<T extends ObjectData> extends DocumentOptions {

        private final InstanceCreator<T> instanceCreator;

        ObjectOptions(boolean useGlobalCache, boolean useLocalCache, InstanceCreator<T> instanceCreator) {
            super(useGlobalCache, useLocalCache);
            this.instanceCreator = instanceCreator;
        }

        public InstanceCreator<T> instanceCreator() {
            return this.instanceCreator;
        }
    }
}
