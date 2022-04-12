package de.natrox.pipeline.datatype.connection;

import com.google.common.base.Preconditions;
import de.natrox.common.concurrent.SimpleTaskBatchFactory;
import de.natrox.common.concurrent.TaskBatch;
import de.natrox.common.runnable.CatchingRunnable;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ConnectionDataLoader {

    private final Pipeline pipeline;
    private final TaskBatch.Factory taskBatchFactory;

    public ConnectionDataLoader(@NotNull Pipeline pipeline) {
        Preconditions.checkNotNull(pipeline, "pipeline");
        this.pipeline = pipeline;
        this.taskBatchFactory = new SimpleTaskBatchFactory();
    }

    public final void loadConnectionData(@NotNull UUID uuid, @Nullable Runnable callback) {
        Preconditions.checkNotNull(uuid, "uuid");

        taskBatchFactory
            .createTaskBatch()
            .async(new CatchingRunnable(() -> {
                pipeline.registry()
                    .dataClasses()
                    .parallelStream()
                    .filter(ConnectionData.class::isAssignableFrom)
                    .forEach(aClass -> {
                        var optional = AnnotationResolver.preload(aClass);

                        optional.ifPresent(preload -> {
                            pipeline
                                .load(aClass, uuid, Pipeline.LoadingStrategy.LOAD_PIPELINE, true)
                                .map(pipelineData -> (ConnectionData) pipelineData)
                                .ifPresent(ConnectionData::onConnect);
                        });
                    });
                callback.run();
            })).execute();
    }

    public final void removeConnectionData(@NotNull UUID uuid, Runnable callback) {
        Preconditions.checkNotNull(uuid, "uuid");

        taskBatchFactory
            .createTaskBatch()
            .async(new CatchingRunnable(() -> {
                pipeline.registry()
                    .dataClasses()
                    .parallelStream()
                    .filter(ConnectionData.class::isAssignableFrom)
                    .forEach(aClass -> {
                        var optional = AnnotationResolver.autoSave(aClass);

                        optional.ifPresent(unload -> {
                            var data = (ConnectionData) pipeline.localCache().data(aClass, uuid);
                            if (data == null)
                                return;

                            pipeline.cleanUpData(aClass, data.objectUUID(), null);
                        });
                    });
                callback.run();
            })).execute();
    }
}
