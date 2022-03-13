package de.notion.pipeline.part.local.updater;

import com.google.gson.JsonObject;
import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public interface DataUpdater {

    /**
     * Pushes the local data to Pipeline
     */
    void pushUpdate(@NotNull PipelineData pipelineData, @Nullable Runnable callback);

    /**
     * Notifies other Servers that hold this data to delete it from local Cache
     */
    void pushRemoval(@NotNull PipelineData pipelineData, @Nullable Runnable callback);

    void registerLoadingTask(@NotNull UUID objectUUID);

    void receivedData(@NotNull UUID objectUUID, JsonObject data);

    @NotNull
    Optional<PipelineData> finishLoadingTask(@NotNull PipelineData pipelineData);

    class RemoveDataBlock extends DataBlock {
        public RemoveDataBlock(@NotNull UUID senderUUID, @NotNull UUID dataUUID) {
            super(senderUUID, dataUUID);
        }
    }

    abstract class DataBlock implements Serializable {

        public final UUID senderUUID;
        public final UUID dataUUID;

        DataBlock(@NotNull UUID senderUUID, @NotNull UUID dataUUID) {
            this.senderUUID = senderUUID;
            this.dataUUID = dataUUID;
        }
    }

    class UpdateDataBlock extends DataBlock {

        public final String dataToUpdate;

        public UpdateDataBlock(@NotNull UUID senderUUID, @NotNull UUID dataUUID, String dataToUpdate) {
            super(senderUUID, dataUUID);
            this.dataToUpdate = dataToUpdate;
        }
    }
}
