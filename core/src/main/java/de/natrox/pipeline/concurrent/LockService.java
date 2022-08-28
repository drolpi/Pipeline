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

package de.natrox.pipeline.concurrent;

import de.natrox.common.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class LockService {

    private final Map<String, ReentrantReadWriteLock> lockRegistry;

    public LockService() {
        this.lockRegistry = new HashMap<>();
    }

    public synchronized @NotNull Lock getReadLock(@NotNull String name) {
        Check.notNull(name, "name");
        if (this.lockRegistry.containsKey(name)) {
            ReentrantReadWriteLock rwLock = this.lockRegistry.get(name);
            return rwLock.readLock();
        }

        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        this.lockRegistry.put(name, rwLock);
        return rwLock.readLock();
    }

    public synchronized @NotNull Lock getReadLock(@NotNull Class<?> type) {
        Check.notNull(type, "type");
        return this.getReadLock(type.getName());
    }

    public synchronized @NotNull Lock getWriteLock(@NotNull String name) {
        Check.notNull(name, "name");
        if (this.lockRegistry.containsKey(name)) {
            ReentrantReadWriteLock rwLock = this.lockRegistry.get(name);
            return rwLock.writeLock();
        }

        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        this.lockRegistry.put(name, rwLock);
        return rwLock.writeLock();
    }

    public synchronized @NotNull Lock getWriteLock(@NotNull Class<?> type) {
        Check.notNull(type, "type");
        return this.getWriteLock(type.getName());
    }
}
