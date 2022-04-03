package de.natrox.pipeline.automatic.load;

import de.natrox.common.concurrent.TaskBatch;
import de.natrox.common.runnable.CatchingRunnable;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.datatype.ConnectionPipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class ConnectionDataLoader {

    private final Pipeline pipeline;

    public ConnectionDataLoader(@NotNull Pipeline pipeline) {
        Objects.requireNonNull(pipeline, "pipeline can't be null!");
        this.pipeline = pipeline;
    }

    public final void loadConnectionData(@NotNull UUID connection, Runnable callback) {
        Objects.requireNonNull(connection, "player can't be null!");

        new TaskBatch().doAsync(new CatchingRunnable(() -> {
            pipeline.registry()
                .dataClasses()
                .parallelStream()
                .filter(ConnectionPipelineData.class::isAssignableFrom)
                .forEach(aClass -> {
                    var optional = AnnotationResolver.preload(aClass);

                    optional.ifPresent(preload -> {
                        var data = (ConnectionPipelineData) pipeline.load(aClass, connection, Pipeline.LoadingStrategy.LOAD_PIPELINE, true);
                        if (data == null)
                            return;

                        data.onConnect();
                    });
                });
            callback.run();
        })).executeBatch();
    }

    public final void removeConnectionData(@NotNull UUID connection, Runnable callback) {
        Objects.requireNonNull(connection, "player can't be null!");

        new TaskBatch().doAsync(new CatchingRunnable(() -> {
            pipeline.registry()
                .dataClasses()
                .parallelStream()
                .filter(ConnectionPipelineData.class::isAssignableFrom)
                .forEach(aClass -> {
                    var optional = AnnotationResolver.autoSave(aClass);

                    optional.ifPresent(unload -> {
                        var data = (ConnectionPipelineData) pipeline.localCache().data(aClass, connection);
                        if (data == null)
                            return;

                        pipeline.cleanUpData(aClass, data.objectUUID(), null);
                    });
                });
            callback.run();
        })).executeBatch();
    }
}
