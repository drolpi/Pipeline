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

package de.natrox.pipeline.cleanup;

import de.natrox.pipeline.PipelineImpl;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.part.local.LocalCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public final class CleanUpTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(CleanUpTask.class);

    private final PipelineImpl pipelineImpl;
    private final PipelineRegistry registry;
    private final LocalCache localCache;

    public CleanUpTask(PipelineImpl pipelineImpl) {
        this.pipelineImpl = pipelineImpl;
        this.registry = pipelineImpl.registry();
        this.localCache = pipelineImpl.localCache();
    }

    @Override
    public void run() {
        for (var dataClass : registry.dataClasses()) {
            AnnotationResolver.cleanUp(dataClass).ifPresent(cleanUp -> {
                for (var uuid : localCache.keys(dataClass)) {
                    var data = localCache.get(dataClass, uuid);
                    if (data == null)
                        return;
                    if ((System.currentTimeMillis() - data.lastUse()) < Duration.of(cleanUp.time(), cleanUp.timeUnit()).toMillis())
                        return;

                    LOGGER.debug("Cleaning up " + dataClass.getSimpleName() + " with uuid " + uuid.toString());
                    data.onCleanUp();
                    pipelineImpl.cleanUpData(dataClass, data.objectUUID(), null);
                }
            });
        }
    }
}
