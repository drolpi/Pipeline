package de.notion.pipeline.part.updater;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class AbstractDataUpdater implements DataUpdater {

    private final Cache<UUID, Optional<JsonObject>> tasks;

    public AbstractDataUpdater() {
        this.tasks = CacheBuilder
                .newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build();
    }

    public void registerLoadingTask(@NotNull UUID objectUUID) {
        tasks.put(objectUUID, Optional.empty());
    }

    public void receivedData(@NotNull UUID objectUUID, JsonObject data) {
        if (tasks.asMap().containsKey(objectUUID)) {
            tasks.put(objectUUID, Optional.ofNullable(data));
            System.out.println("Received Sync while loading " + System.currentTimeMillis()); //DEBUG
        }
    }

    @NotNull
    public Optional<PipelineData> finishLoadingTask(@NotNull PipelineData pipelineData) {
        var objectUUID = pipelineData.objectUUID();
        var map = tasks.asMap();

        if (!map.containsKey(objectUUID))
            return null;

        var optional = map.get(objectUUID);
        return optional.map(data -> {
            map.remove(objectUUID);
            return pipelineData.deserialize(data);
        });
    }

}
