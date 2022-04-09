package de.natrox.pipeline.datatype.connection;

import de.natrox.common.concurrent.SimpleTaskBatchFactory;
import de.natrox.common.concurrent.TaskBatch;
import de.natrox.common.runnable.CatchingRunnable;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class ConnectionDataLoader {

    private final Pipeline pipeline;
    private final TaskBatch.Factory taskBatchFactory;

    public ConnectionDataLoader(@NotNull Pipeline pipeline) {
        Objects.requireNonNull(pipeline, "pipeline can't be null!");
        this.pipeline = pipeline;
        this.taskBatchFactory = new SimpleTaskBatchFactory();
    }

    public final void loadConnectionData(@NotNull UUID connection, Runnable callback) {
        Objects.requireNonNull(connection, "player can't be null!");

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
                            var data = (ConnectionData) pipeline.load(aClass, connection, Pipeline.LoadingStrategy.LOAD_PIPELINE, true);
                            if (data == null)
                                return;

                            data.onConnect();
                        });
                    });
                callback.run();
            })).execute();
    }

    public final void removeConnectionData(@NotNull UUID connection, Runnable callback) {
        Objects.requireNonNull(connection, "player can't be null!");

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
                            var data = (ConnectionData) pipeline.localCache().data(aClass, connection);
                            if (data == null)
                                return;

                            pipeline.cleanUpData(aClass, data.objectUUID(), null);
                        });
                    });
                callback.run();
            })).execute();
    }
}
