package de.notion.pipeline.scheduler;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.PipelineManager;
import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PipelineTaskSchedulerImpl implements PipelineTaskScheduler {

    private final PipelineManager pipelineManager;
    private final Map<UUID, Map<Class<? extends PipelineData>, PipelineTask<?>>> pendingTasks = new ConcurrentHashMap<>();

    public PipelineTaskSchedulerImpl(@NotNull PipelineManager pipelineManager) {
        Objects.requireNonNull(pipelineManager, "pipelineManager can't be null!");
        this.pipelineManager = pipelineManager;
    }

    @Override
    public synchronized <T extends PipelineData> PipelineTask<T> schedulePipelineTask(@NotNull PipelineAction pipelineAction, @NotNull Pipeline.LoadingStrategy loadingStrategy, @NotNull Class<? extends T> type, @NotNull(exception = IllegalArgumentException.class) UUID uuid) {
        Objects.requireNonNull(type, "type can't be null!");
        Objects.requireNonNull(uuid, "uuid can't be null!");
        PipelineTask<T> existingTask = getExistingPipelineTask(type, uuid);
        if (existingTask != null) {
            System.out.println("[" + loadingStrategy + "] Found existing Pipeline Task: " + existingTask); //DEBUG
            return existingTask;
        }
        PipelineTask<T> pipelineTask = new PipelineTask<>(this, pipelineAction, type, uuid, () -> removePipelineTask(type, uuid));
        //System.out.println("[" + loadingStrategy + "] Scheduling Pipeline Task: " + pipelineTask); //DEBUG

        if (!pendingTasks.containsKey(uuid))
            pendingTasks.put(uuid, new ConcurrentHashMap<>());
        pendingTasks.get(uuid).put(type, pipelineTask);
        return pipelineTask;
    }

    @Override
    public synchronized <T extends PipelineData> PipelineTask<T> getExistingPipelineTask(@NotNull Class<? extends T> type, @NotNull UUID uuid) {
        Objects.requireNonNull(type, "type can't be null!");
        Objects.requireNonNull(uuid, "uuid can't be null!");
        if (!pendingTasks.containsKey(uuid))
            return null;
        Map<Class<? extends PipelineData>, PipelineTask<?>> map = pendingTasks.get(uuid);
        if (!map.containsKey(type))
            return null;
        PipelineTask<?> task = map.get(type);
        return (PipelineTask<T>) task;
    }

    @Override
    public synchronized <T extends PipelineData> void removePipelineTask(@NotNull Class<? extends T> type, @NotNull UUID uuid) {
        Objects.requireNonNull(type, "type can't be null!");
        Objects.requireNonNull(uuid, "uuid can't be null!");
        if (!pendingTasks.containsKey(uuid))
            return;
        pendingTasks.get(uuid).remove(type);
        if (pendingTasks.get(uuid).isEmpty())
            pendingTasks.remove(uuid);
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public void shutdown() {
        System.out.println("Shutting down Pipeline Task Scheduler");
        pendingTasks.forEach((uuid, pipelineTasks) -> {
            pipelineTasks.forEach((aClass, pipelineTask) -> {
                try {
                    pipelineTask.getCompletableFuture().get(1, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    System.out.println("Pipeline Task took too long for type: " + Arrays.toString(pipelineTask.getCompletableFuture().getClass().getGenericInterfaces()));
                    e.printStackTrace();
                }
            });
        });
        System.out.println("Pipeline Task Scheduler shut down successfully");
    }
}
