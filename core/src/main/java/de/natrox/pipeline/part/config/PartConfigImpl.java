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

import java.time.Duration;

final class PartConfigImpl {

    abstract static sealed class AbstractPartConfig implements PartConfig {

    }

    abstract static sealed class AbstractStorageConfig extends AbstractPartConfig implements PartConfig.Storage {

    }

    abstract static sealed class AbstractCacheConfig extends AbstractPartConfig implements PartConfig.Cache {

        private final Duration expireAfterWrite;
        private final Duration expireAfterAccess;

        private AbstractCacheConfig(Duration expireAfterWrite, Duration expireAfterAccess) {
            this.expireAfterWrite = expireAfterWrite;
            this.expireAfterAccess = expireAfterAccess;
        }

        @Override
        public @NotNull Duration expireAfterWrite() {
            return this.expireAfterWrite;
        }

        @Override
        public @NotNull Duration expireAfterAccess() {
            return this.expireAfterAccess;
        }
    }

    final static class GlobalStorageConfigImpl extends AbstractStorageConfig implements GlobalStorageConfig {

    }

    final static class LocalStorageConfigImpl extends AbstractStorageConfig implements LocalStorageConfig {

    }

    final static class GlobalCacheConfigImpl extends AbstractCacheConfig implements GlobalCacheConfig {

        GlobalCacheConfigImpl(Duration expireAfterWrite, Duration expireAfterAccess) {
            super(expireAfterWrite, expireAfterAccess);
        }
    }

    final static class LocalCacheConfigImpl extends AbstractCacheConfig implements LocalCacheConfig {

        LocalCacheConfigImpl(Duration expireAfterWrite, Duration expireAfterAccess) {
            super(expireAfterWrite, expireAfterAccess);
        }
    }

}
