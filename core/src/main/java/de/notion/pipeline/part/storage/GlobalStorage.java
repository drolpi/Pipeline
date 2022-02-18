package de.notion.pipeline.part.storage;

import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.filter.Filter;
import de.notion.pipeline.part.DataProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public interface GlobalStorage extends DataProvider {

    List<UUID> filter(@NotNull Class<? extends PipelineData> type, @NotNull Filter filter);

}
