package de.notion.pipeline.scheduler;

import de.notion.common.system.SystemLoadable;
import de.notion.pipeline.Pipeline;
import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PipelineTaskScheduler extends SystemLoadable {

    <T extends PipelineData> PipelineTask<T> schedulePipelineTask(@NotNull PipelineAction pipelineAction, @NotNull Pipeline.LoadingStrategy loadingStrategy, @NotNull Class<? extends T> type, @NotNull UUID uuid);

    <T extends PipelineData> PipelineTask<T> getExistingPipelineTask(@NotNull Class<? extends T> type, @NotNull UUID uuid);

    <T extends PipelineData> void removePipelineTask(@NotNull Class<? extends T> type, @NotNull UUID uuid);

    enum PipelineAction {
        LOAD
    }

    class PipelineTask<T extends PipelineData> {
        private final PipelineAction pipelineAction;
        private final Class<? extends PipelineData> type;
        private final UUID uuid;
        private final CompletableFuture<T> completableFuture;
        private final UUID taskUUID = UUID.randomUUID();
        private final long start = System.currentTimeMillis();

        public PipelineTask(PipelineTaskScheduler pipelineTaskScheduler, PipelineAction pipelineAction, Class<? extends T> type, UUID uuid, Runnable onComplete) {
            this.pipelineAction = pipelineAction;
            this.type = type;
            this.uuid = uuid;
            this.completableFuture = new CompletableFuture<>();
            this.completableFuture.whenComplete((t, throwable) -> {
                //System.out.println("Task " + taskUUID + " done: " + type + "  |  " + getObjectUUID() + " [" + t + "] [" + (System.currentTimeMillis() - start) + "ms]"); //DEBUG
                onComplete.run();
                pipelineTaskScheduler.removePipelineTask(type, uuid);
            });
        }

        public Class<? extends PipelineData> getType() {
            return type;
        }

        public UUID getObjectUUID() {
            return uuid;
        }

        public PipelineAction getPipelineAction() {
            return pipelineAction;
        }

        public CompletableFuture<T> getCompletableFuture() {
            return completableFuture;
        }

        public UUID getTaskUUID() {
            return taskUUID;
        }
    }
}
