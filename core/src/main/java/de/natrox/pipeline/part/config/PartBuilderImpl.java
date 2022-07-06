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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
final class PartBuilderImpl {

    abstract static class AbstractPartBuilder<T extends PartConfig, R extends PartConfig.Builder<T, R>> implements PartConfig.Builder<T, R> {

    }

    abstract static class AbstractStorageBuilder<T extends PartConfig.Storage, R extends PartConfig.StorageBuilder<T, R>> implements PartConfig.StorageBuilder<T, R> {

    }

    abstract static class AbstractCacheBuilder<T extends PartConfig.Cache, R extends PartConfig.CacheBuilder<T, R>> implements PartConfig.CacheBuilder<T, R> {

        protected long expireAfterWriteNanos = -1;
        protected long expireAfterAccessNanos = -1;

        @Override
        public @NotNull R expireAfterWrite(long time, @NotNull TimeUnit unit) {
            this.expireAfterWriteNanos = unit.toNanos(time);
            return (R) this;
        }

        @Override
        public @NotNull R expireAfterAccess(long time, @NotNull TimeUnit unit) {
            this.expireAfterAccessNanos = unit.toNanos(time);
            return (R) this;
        }

    }

    static class GlobalStorageBuilder extends AbstractStorageBuilder<GlobalStorageConfig, GlobalStorageConfig.Builder> implements GlobalStorageConfig.Builder {

        @Override
        public @UnknownNullability GlobalStorageConfig build() {
            return new PartConfigImpl.GlobalStorageConfigImpl();
        }
    }

    static class LocalStorageBuilder extends AbstractStorageBuilder<LocalStorageConfig, LocalStorageConfig.Builder> implements LocalStorageConfig.Builder {

        @Override
        public @UnknownNullability LocalStorageConfig build() {
            return new PartConfigImpl.LocalStorageConfigImpl();
        }
    }

    static class GlobalCacheBuilder extends AbstractCacheBuilder<GlobalCacheConfig, GlobalCacheConfig.Builder> implements GlobalCacheConfig.Builder {

        @Override
        public @UnknownNullability GlobalCacheConfig build() {
            return new PartConfigImpl.GlobalCacheConfigImpl(this.expireAfterWriteNanos, this.expireAfterAccessNanos);
        }
    }

    static class LocalCacheBuilder extends AbstractCacheBuilder<LocalCacheConfig, LocalCacheConfig.Builder> implements LocalCacheConfig.Builder {

        @Override
        public @UnknownNullability LocalCacheConfig build() {
            return new PartConfigImpl.LocalCacheConfigImpl(this.expireAfterWriteNanos, this.expireAfterAccessNanos);
        }
    }

}
