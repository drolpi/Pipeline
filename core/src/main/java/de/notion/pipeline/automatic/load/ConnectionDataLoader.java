package de.notion.pipeline.automatic.load;

import de.notion.common.concurrent.TaskBatch;
import de.notion.pipeline.Pipeline;
import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.datatype.ConnectionPipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class ConnectionDataLoader {

    private final Pipeline pipeline;

    public ConnectionDataLoader(@NotNull Pipeline pipeline) {
        Objects.requireNonNull(pipeline, "pipeline can't be null!");
        this.pipeline = pipeline;
    }

    protected final void loadConnectionData(@NotNull UUID connection, Runnable callback) {
        Objects.requireNonNull(connection, "player can't be null!");

        new TaskBatch().doAsync(() -> {
                    pipeline.registry()
                            .dataClasses()
                            .parallelStream()
                            .filter(aClass -> ConnectionPipelineData.class.isAssignableFrom(aClass))
                            .forEach(aClass -> {
                                var optional = AnnotationResolver.preload(aClass);

                                optional.ifPresent(preload -> {
                                    var data = (ConnectionPipelineData) pipeline.load(aClass, connection, Pipeline.LoadingStrategy.LOAD_PIPELINE, preload.creationStrategies());
                                    if (data == null)
                                        return;

                                    data.onConnect();
                                });
                            });
                    callback.run();
                }
        ).executeBatch();
    }

    protected final void removeConnectionData(@NotNull UUID connection, Runnable callback) {
        Objects.requireNonNull(connection, "player can't be null!");

        new TaskBatch().doAsync(() -> {
                    pipeline.registry()
                            .dataClasses()
                            .parallelStream()
                            .filter(aClass -> ConnectionPipelineData.class.isAssignableFrom(aClass))
                            .forEach(aClass -> {
                                var optional = AnnotationResolver.autoSave(aClass);

                                optional.ifPresent(unload -> {
                                    var data = (ConnectionPipelineData) pipeline.localCache().data(aClass, connection);
                                    if (data == null)
                                        return;

                                    data.onDisconnect();
                                    pipeline.saveData(aClass, data.objectUUID(), null);
                                });
                            });
                    callback.run();
                }
        ).executeBatch();
    }
}
