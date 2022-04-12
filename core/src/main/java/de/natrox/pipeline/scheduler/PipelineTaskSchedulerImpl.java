package de.natrox.pipeline.scheduler;

import com.google.common.base.Preconditions;
import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class PipelineTaskSchedulerImpl implements PipelineTaskScheduler {

    private final static Logger LOGGER = LogManager.logger(PipelineTaskSchedulerImpl.class);

    private final Map<UUID, Map<Class<? extends PipelineData>, PipelineTask<?>>> pendingTasks = new ConcurrentHashMap<>();

    @Override
    public synchronized <T extends PipelineData> @NotNull PipelineTask<T> schedule(@NotNull PipelineAction pipelineAction, @NotNull Pipeline.LoadingStrategy loadingStrategy, @NotNull Class<? extends T> type, @NotNull(exception = IllegalArgumentException.class) UUID uuid) {
        Preconditions.checkNotNull(type, "type");
        Preconditions.checkNotNull(uuid, "uuid");
        PipelineTask<T> existingTask = pipelineTask(type, uuid);
        if (existingTask != null) {
            return existingTask;
        }
        var pipelineTask = new PipelineTask<T>(this, pipelineAction, type, uuid, () -> remove(type, uuid));

        if (!pendingTasks.containsKey(uuid))
            pendingTasks.put(uuid, new ConcurrentHashMap<>());
        pendingTasks.get(uuid).put(type, pipelineTask);
        return pipelineTask;
    }

    @Override
    public synchronized <T extends PipelineData> PipelineTask<T> pipelineTask(@NotNull Class<? extends T> type, @NotNull UUID uuid) {
        Preconditions.checkNotNull(type, "type");
        Preconditions.checkNotNull(uuid, "uuid");
        if (!pendingTasks.containsKey(uuid))
            return null;
        var map = pendingTasks.get(uuid);
        if (!map.containsKey(type))
            return null;
        var task = map.get(type);
        return (PipelineTask<T>) task;
    }

    @Override
    public synchronized <T extends PipelineData> void remove(@NotNull Class<? extends T> type, @NotNull UUID uuid) {
        Preconditions.checkNotNull(type, "type");
        Preconditions.checkNotNull(uuid, "uuid");
        if (!pendingTasks.containsKey(uuid))
            return;
        pendingTasks.get(uuid).remove(type);
        if (pendingTasks.get(uuid).isEmpty())
            pendingTasks.remove(uuid);
    }

    @Override
    public void load() {

    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public void shutdown() {
        LOGGER.debug("Shutting down Pipeline Task Scheduler");
        pendingTasks.forEach((uuid, pipelineTasks) -> {
            pipelineTasks.forEach((aClass, pipelineTask) -> {
                try {
                    pipelineTask.completableFuture().get(1, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    LOGGER.warning("Pipeline Task took too long for type: " + Arrays.toString(pipelineTask.completableFuture().getClass().getGenericInterfaces()));
                    e.printStackTrace();
                }
            });
        });
        LOGGER.debug("Pipeline Task Scheduler shut down successfully");
    }
}
