package de.notion.pipeline.part.storage;

import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.operator.filter.Filter;
import de.notion.pipeline.part.DataProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface GlobalStorage extends DataProvider {

    List<UUID> filteredUUIDs(@NotNull Class<? extends PipelineData> type, @NotNull Filter filter);

    List<UUID> sortedUUIDs(@NotNull Class<? extends PipelineData> type);

}
