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

final class StoreMapConfigImpl {

    static abstract sealed class AbstractStoreMapConfig implements StoreMapConfig {

    }

    final static class StorageConfigImpl extends AbstractStoreMapConfig implements StorageConfig {

        final static StorageConfig DEFAULT = StorageConfig.builder().build();

    }

    static abstract sealed class AbstractCacheConfig extends AbstractStoreMapConfig implements CacheConfig {

        private final long expireAfterWriteNanos;
        private final long expireAfterAccessNanos;

        private AbstractCacheConfig(long expireAfterWriteNanos, long expireAfterAccessNanos) {
            this.expireAfterWriteNanos = expireAfterWriteNanos;
            this.expireAfterAccessNanos = expireAfterAccessNanos;
        }

        @Override
        public long expireAfterWriteNanos() {
            return this.expireAfterWriteNanos;
        }

        @Override
        public long expireAfterAccessNanos() {
            return this.expireAfterAccessNanos;
        }
    }

    final static class GlobalCacheConfigImpl extends AbstractCacheConfig implements GlobalCacheConfig {

        final static GlobalCacheConfig DEFAULT = GlobalCacheConfig.builder().build();

        GlobalCacheConfigImpl(long expireAfterWriteNanos, long expireAfterAccessNanos) {
            super(expireAfterWriteNanos, expireAfterAccessNanos);
        }
    }

    final static class LocalCacheConfigImpl extends AbstractCacheConfig implements LocalCacheConfig {

        final static LocalCacheConfig DEFAULT = LocalCacheConfig.builder().build();

        LocalCacheConfigImpl(long expireAfterWriteNanos, long expireAfterAccessNanos) {
            super(expireAfterWriteNanos, expireAfterAccessNanos);
        }
    }
}
