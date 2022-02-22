package de.notion.pipeline.part.local.updater;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class AbstractDataUpdater implements DataUpdater {

    protected final Cache<UUID, Optional<String>> tasks;

    public AbstractDataUpdater() {
        this.tasks = CacheBuilder
                .newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build();
    }

    public void registerLoadingTask(@NotNull UUID objectUUID) {
        tasks.put(objectUUID, Optional.empty());
    }

    public void registerSyncedData(@NotNull UUID objectUUID, String data) {
        tasks.put(objectUUID, Optional.ofNullable(data));
    }

    public void finishLoadingTask(@NotNull PipelineData pipelineData) {
        var objectUUID = pipelineData.objectUUID();
        var map = tasks.asMap();

        if (!map.containsKey(objectUUID))
            return;

        var optional = map.get(objectUUID);
        optional.ifPresent(data -> {
            pipelineData.onSync(pipelineData.deserialize(data));
            map.remove(objectUUID);
        });
    }

    public static class RemoveDataBlock extends DataBlock {
        public RemoveDataBlock(@NotNull UUID senderUUID, @NotNull UUID dataUUID) {
            super(senderUUID, dataUUID);
        }
    }

    public abstract static class DataBlock implements Serializable {
        public final UUID senderUUID;
        public final UUID dataUUID;

        DataBlock(@NotNull UUID senderUUID, @NotNull UUID dataUUID) {
            this.senderUUID = senderUUID;
            this.dataUUID = dataUUID;
        }
    }

    public static class UpdateDataBlock extends DataBlock {
        public final String dataToUpdate;

        public UpdateDataBlock(@NotNull UUID senderUUID, @NotNull UUID dataUUID, String dataToUpdate) {
            super(senderUUID, dataUUID);
            this.dataToUpdate = dataToUpdate;
        }
    }

}
