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

package de.natrox.pipeline.repository.option;

import org.jetbrains.annotations.NotNull;

abstract sealed class AbstractOptions implements Options permits DocumentOptionsImpl {

    private final boolean useGlobalCache;
    private final boolean useLocalCache;

    AbstractOptions(boolean useGlobalCache, boolean useLocalCache) {
        this.useGlobalCache = useGlobalCache;
        this.useLocalCache = useLocalCache;
    }

    @Override
    public boolean useGlobalCache() {
        return this.useGlobalCache;
    }

    @Override
    public boolean useLocalCache() {
        return this.useLocalCache;
    }

    @SuppressWarnings("unchecked")
    abstract static non-sealed class AbstractBuilder<T extends Options, R extends Options.OptionsBuilder<T, R>> implements Options.OptionsBuilder<T, R> {

        protected boolean useGlobalCache;
        protected boolean useLocalCache;

        @Override
        public @NotNull R useGlobalCache(boolean use) {
            this.useGlobalCache = use;
            return (R) this;
        }

        @Override
        public @NotNull R useLocalCache(boolean use) {
            this.useLocalCache = use;
            return (R) this;
        }
    }

}
