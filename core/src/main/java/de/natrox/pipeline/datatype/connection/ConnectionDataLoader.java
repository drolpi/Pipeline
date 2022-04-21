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

package de.natrox.pipeline.datatype.connection;

import de.natrox.common.batch.SimpleTaskBatchFactory;
import de.natrox.common.batch.TaskBatch;
import de.natrox.common.runnable.CatchingRunnable;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ConnectionDataLoader {

    private final Pipeline pipeline;
    private final TaskBatch.Factory taskBatchFactory;

    public ConnectionDataLoader(@NotNull Pipeline pipeline) {
        Check.notNull(pipeline, "pipeline");
        this.pipeline = pipeline;
        this.taskBatchFactory = new SimpleTaskBatchFactory();
    }

    public final void loadConnectionData(@NotNull UUID uuid, @Nullable Runnable callback) {
        Check.notNull(uuid, "uuid");

        this.taskBatchFactory
            .createTaskBatch()
            .async(new CatchingRunnable(() -> {
                this.pipeline.registry()
                    .dataClasses()
                    .parallelStream()
                    .filter(ConnectionData.class::isAssignableFrom)
                    .forEach(aClass -> {
                        var optional = AnnotationResolver.preload(aClass);

                        optional.flatMap(preload -> this.pipeline
                            .load(aClass, uuid, Pipeline.LoadingStrategy.LOAD_PIPELINE, true)
                            .map(pipelineData -> (ConnectionData) pipelineData))
                            .ifPresent(ConnectionData::onConnect);
                    });
                if (callback != null)
                    callback.run();
            })).execute();
    }

    public final void removeConnectionData(@NotNull UUID uuid, @Nullable Runnable callback) {
        Check.notNull(uuid, "uuid");

        this.taskBatchFactory
            .createTaskBatch()
            .async(new CatchingRunnable(() -> {
                this.pipeline.registry()
                    .dataClasses()
                    .parallelStream()
                    .filter(ConnectionData.class::isAssignableFrom)
                    .forEach(aClass -> {
                        var optional = AnnotationResolver.autoSave(aClass);

                        optional.ifPresent(unload -> {
                            var data = (ConnectionData) this.pipeline.localCache().get(aClass, uuid);
                            if (data == null)
                                return;

                            this.pipeline.cleanUpData(aClass, data.objectUUID(), null);
                        });
                    });
                if (callback != null)
                    callback.run();
            })).execute();
    }
}
