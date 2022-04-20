package de.natrox.pipeline.part.updater;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.json.document.JsonDocument;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractDataUpdater implements DataUpdater {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractDataUpdater.class);

    protected final Cache<UUID, JsonDocument> syncs;

    public AbstractDataUpdater() {
        this.syncs = CacheBuilder
            .newBuilder()
            .expireAfterWrite(Duration.of(30, ChronoUnit.SECONDS))
            .build();
    }

    public void receivedSync(@NotNull UUID objectUUID, JsonDocument data) {
        syncs.put(objectUUID, data);
        LOGGER.debug("Received Sync while loading " + System.currentTimeMillis());
    }

    @NotNull
    public Optional<PipelineData> applySync(@NotNull PipelineData pipelineData) {
        var objectUUID = pipelineData.objectUUID();
        var map = syncs.asMap();

        if (!map.containsKey(objectUUID))
            return Optional.empty();

        var data = map.get(objectUUID);
        map.remove(objectUUID);
        return Optional.of(pipelineData.deserialize(data));
    }

}
