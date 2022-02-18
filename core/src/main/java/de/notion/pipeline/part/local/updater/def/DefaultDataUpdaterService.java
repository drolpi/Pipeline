package de.notion.pipeline.part.local.updater.def;

import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.local.updater.DataUpdater;
import de.notion.pipeline.part.local.updater.DataUpdaterService;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DefaultDataUpdaterService implements DataUpdaterService {

    private final Map<Class<? extends PipelineData>, DataUpdater> cache;

    public DefaultDataUpdaterService() {
        this.cache = new HashMap<>();
        System.out.println("Default DataUpdaterService started");
    }

    @Override
    public DataUpdater dataUpdater(@NotNull Class<? extends PipelineData> dataClass) {
        cache.putIfAbsent(dataClass, new DefaultDataUpdater());
        return cache.get(dataClass);
    }
}
