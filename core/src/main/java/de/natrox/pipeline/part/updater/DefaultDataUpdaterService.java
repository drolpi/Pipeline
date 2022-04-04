package de.natrox.pipeline.part.updater;

import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class DefaultDataUpdaterService implements DataUpdaterService {

    private final static Logger LOGGER = LogManager.logger(DefaultDataUpdaterService.class);

    private final Map<Class<? extends PipelineData>, DataUpdater> cache;

    public DefaultDataUpdaterService() {
        this.cache = new HashMap<>();
        LOGGER.debug("Default DataUpdaterService started"); 
    }

    @NotNull
    @Override
    public DataUpdater dataUpdater(@NotNull Class<? extends PipelineData> dataClass) {
        if (!cache.containsKey(dataClass))
            cache.put(dataClass, new DefaultDataUpdater());
        return cache.get(dataClass);
    }
}
