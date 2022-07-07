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

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
abstract non-sealed class AbstractRepositoryBuilder<T extends Repository<?>, R extends Repository.Builder<T, R>> implements Repository.Builder<T, R> {

    protected boolean useGlobalCache = true;
    protected boolean useLocalCache = true;

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
