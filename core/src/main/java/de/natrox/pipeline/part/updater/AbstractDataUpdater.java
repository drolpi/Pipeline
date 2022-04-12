package de.natrox.pipeline.part.updater;

import com.google.gson.JsonObject;
import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDataUpdater implements DataUpdater {

    private final static Logger LOGGER = LogManager.logger(AbstractDataUpdater.class);

    protected final Map<UUID, JsonObject> syncs;

    public AbstractDataUpdater() {
        this.syncs = new ConcurrentHashMap<>();
    }

    public void receivedSync(@NotNull UUID objectUUID, JsonObject data) {
        syncs.put(objectUUID, data);
        LOGGER.debug("Received Sync while loading " + System.currentTimeMillis());
    }

    @NotNull
    public Optional<PipelineData> applySync(@NotNull PipelineData pipelineData) {
        var objectUUID = pipelineData.objectUUID();

        if (!syncs.containsKey(objectUUID))
            return Optional.empty();

        var data = syncs.get(objectUUID);
        syncs.remove(objectUUID);
        return Optional.ofNullable(pipelineData.deserialize(data));
    }

}
