package de.natrox.pipeline.scheduler;

import de.natrox.common.Loadable;
import de.natrox.common.Shutdownable;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PipelineTaskScheduler extends Loadable, Shutdownable {

    <T extends PipelineData> @NotNull PipelineTask<T> schedule(@NotNull PipelineAction pipelineAction, @NotNull Pipeline.LoadingStrategy loadingStrategy, @NotNull Class<? extends T> type, @NotNull UUID uuid);

    <T extends PipelineData> @Nullable PipelineTask<T> pipelineTask(@NotNull Class<? extends T> type, @NotNull UUID uuid);

    <T extends PipelineData> void remove(@NotNull Class<? extends T> type, @NotNull UUID uuid);

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
                onComplete.run();
                pipelineTaskScheduler.remove(type, uuid);
            });
        }

        public Class<? extends PipelineData> type() {
            return type;
        }

        public UUID objectUUID() {
            return uuid;
        }

        public PipelineAction action() {
            return pipelineAction;
        }

        public CompletableFuture<T> completableFuture() {
            return completableFuture;
        }

        public UUID taskUUID() {
            return taskUUID;
        }
    }
}
