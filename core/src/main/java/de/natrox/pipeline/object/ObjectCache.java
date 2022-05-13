/*
 * Copyright 2020-2022 NatroxMC team
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

package de.natrox.pipeline.object;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.document.PipeDocument;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ObjectCache<T extends ObjectData> {

    private final Pipeline pipeline;
    private final Class<T> type;
    private final Map<UUID, T> cache;

    ObjectCache(Pipeline pipeline, Class<T> type) {
        this.pipeline = pipeline;
        this.type = type;
        this.cache = new ConcurrentHashMap<>();
    }

    public T get(UUID uniqueId) {
        if (!this.cache.containsKey(uniqueId)) {
            this.cache.put(uniqueId, create(uniqueId));
        }
        return this.cache.get(uniqueId);
    }

    private @NotNull T create(UUID uniqueId) {
        // FIXME: 13.05.2022
        InstanceCreator<T> instanceCreator = new SimpleInstanceCreator<>();
        T instance;
        try {
            instance = instanceCreator.get(this.type, this.pipeline);
        } catch (Throwable throwable) {
            throw new RuntimeException("Error while creating instance of class " + this.type.getSimpleName(), throwable);
        }

        PipeDocument document = PipeDocument.create();
        document.put("uniqueId", uniqueId);
        instance.deserialize(document);

        return instance;
    }

}
