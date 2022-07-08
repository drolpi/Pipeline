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
final class StoreMapConfigBuilderImpl {

    static abstract class AbstractPartBuilder<T extends StoreMapConfig, R extends StoreMapConfig.Builder<T, R>> implements StoreMapConfig.Builder<T, R> {

    }

    static class StorageBuilder extends AbstractPartBuilder<StorageConfig, StorageConfig.Builder> implements StorageConfig.Builder {

        @Override
        public @UnknownNullability StorageConfig build() {
            return new StoreMapConfigImpl.StorageConfigImpl();
        }
    }

    static abstract class AbstractCacheBuilder<T extends CacheConfig, R extends CacheConfig.Builder<T, R>> implements CacheConfig.Builder<T, R> {

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

    static class GlobalCacheBuilder extends AbstractCacheBuilder<GlobalCacheConfig, GlobalCacheConfig.Builder> implements GlobalCacheConfig.Builder {

        @Override
        public @UnknownNullability GlobalCacheConfig build() {
            return new StoreMapConfigImpl.GlobalCacheConfigImpl(this.expireAfterWriteNanos, this.expireAfterAccessNanos);
        }
    }

    static class LocalCacheBuilder extends AbstractCacheBuilder<LocalCacheConfig, LocalCacheConfig.Builder> implements LocalCacheConfig.Builder {

        @Override
        public @UnknownNullability LocalCacheConfig build() {
            return new StoreMapConfigImpl.LocalCacheConfigImpl(this.expireAfterWriteNanos, this.expireAfterAccessNanos);
        }
    }
}
