package de.notion.pipeline.connection;

import de.notion.common.concurrent.TaskBatch;
import de.notion.common.system.SystemLoadable;
import de.notion.pipeline.Pipeline;
import de.notion.pipeline.annotation.auto.AutoLoad;
import de.notion.pipeline.annotation.auto.AutoSave;
import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.datatype.ConnectionPipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ConnectionManager implements SystemLoadable {

    private final Pipeline pipeline;
    private final boolean loaded;

    public ConnectionManager(@NotNull Pipeline pipelineManager) {
        Objects.requireNonNull(pipelineManager, "pipeline can't be null!");
        this.pipeline = pipelineManager;
        loaded = true;
    }

    private static TaskBatch createTaskBatch() {
        return new TaskBatch();
    }

    protected final void loginPipeline(@NotNull UUID connection, Runnable callback) {
        Objects.requireNonNull(connection, "player can't be null!");

        createTaskBatch()
                .wait(400, TimeUnit.MILLISECONDS)
                .doAsync(() ->
                        pipeline.registry()
                                .dataClasses()
                                .parallelStream()
                                .filter(aClass -> ConnectionPipelineData.class.isAssignableFrom(aClass))
                                .forEach(aClass -> {
                                    AutoLoad autoLoad = AnnotationResolver.autoLoad(aClass);
                                    if (autoLoad == null)
                                        return;

                                    ConnectionPipelineData data = (ConnectionPipelineData) pipeline.load(aClass, connection, Pipeline.LoadingStrategy.LOAD_PIPELINE, autoLoad.creationStrategies());
                                    if (data == null)
                                        return;

                                    data.onConnect();
                                    callback.run();
                                })
                ).executeBatch();
    }

    protected final void logoutPipeline(@NotNull UUID connection, Runnable callback) {
        Objects.requireNonNull(connection, "player can't be null!");

        createTaskBatch()
                .doAsync(() ->
                        pipeline.registry()
                                .dataClasses()
                                .parallelStream()
                                .filter(aClass -> ConnectionPipelineData.class.isAssignableFrom(aClass))
                                .forEach(aClass -> {
                                    AutoSave autoSave = AnnotationResolver.autoSave(aClass);
                                    if (autoSave == null)
                                        return;

                                    ConnectionPipelineData data = (ConnectionPipelineData) pipeline.localCache().data(aClass, connection);
                                    if (data == null)
                                        return;

                                    data.onDisconnect();
                                    pipeline.saveData(aClass, data.objectUUID(), () -> {
                                        pipeline.delete(aClass, data.objectUUID(), Pipeline.QueryStrategy.GLOBAL_CACHE);
                                        callback.run();
                                    });
                                })
                ).executeBatch();
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void shutdown() {

    }
}
