package de.notion.pipeline.operator;

import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.operator.filter.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PipelineStream<T extends PipelineData> {

    @Nullable
    T first();

    @NotNull
    List<T> collect();

    @NotNull
    PipelineStream<T> filter(@NotNull Filter filter);

    @NotNull
    PipelineStream<T> sort(@NotNull Object sorter);

    @NotNull
    PipelineStream<T> limit(int limit);

    @NotNull
    PipelineStream<T> skip(int skip);

}
