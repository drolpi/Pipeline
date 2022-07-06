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

package de.natrox.pipeline.part.config;

import de.natrox.common.builder.IBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@ApiStatus.Experimental
public sealed interface PartConfig permits PartConfig.Cache, PartConfig.Storage, PartConfigImpl.AbstractPartConfig {

    sealed interface Storage extends PartConfig permits GlobalStorageConfig, LocalStorageConfig, PartConfigImpl.AbstractStorageConfig {

    }

    sealed interface Cache extends PartConfig permits GlobalCacheConfig, LocalCacheConfig, PartConfigImpl.AbstractCacheConfig {

        long expireAfterWriteNanos();

        long expireAfterAccessNanos();

    }

    interface Builder<T extends PartConfig, R extends Builder<T, R>> extends IBuilder<T> {

    }

    interface StorageBuilder<T extends Storage, R extends PartConfig.Builder<T, R>> extends PartConfig.Builder<T, R> {

    }

    interface CacheBuilder<T extends Cache, R extends PartConfig.Builder<T, R>> extends PartConfig.Builder<T, R> {

        @NotNull R expireAfterWrite(long time, @NotNull TimeUnit unit);

        @NotNull R expireAfterAccess(long time, @NotNull TimeUnit unit);

    }
}
