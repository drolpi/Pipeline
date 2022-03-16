package de.notion.pipeline.part.updater;

import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DefaultDataUpdaterService implements DataUpdaterService {

    private final Map<Class<? extends PipelineData>, DataUpdater> cache;

    public DefaultDataUpdaterService() {
        this.cache = new HashMap<>();
        System.out.println("Default DataUpdaterService started");
    }

    @NotNull
    @Override
    public DataUpdater dataUpdater(@NotNull Class<? extends PipelineData> dataClass) {
        if (!cache.containsKey(dataClass))
            cache.put(dataClass, new DefaultDataUpdater());
        return cache.get(dataClass);
    }
}
