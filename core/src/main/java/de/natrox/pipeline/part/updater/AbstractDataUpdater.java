package de.natrox.pipeline.part.updater;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class AbstractDataUpdater implements DataUpdater {

    private final static Logger LOGGER = LogManager.logger(AbstractDataUpdater.class);

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
            LOGGER.debug("Received Sync while loading " + System.currentTimeMillis()); 
        }
    }

    @NotNull
    public Optional<PipelineData> finishLoadingTask(@NotNull PipelineData pipelineData) {
        var objectUUID = pipelineData.objectUUID();
        var map = tasks.asMap();

        if (!map.containsKey(objectUUID))
            return Optional.empty();

        var optional = map.get(objectUUID);
        return optional.map(data -> {
            map.remove(objectUUID);
            return pipelineData.deserialize(data);
        });
    }

}
