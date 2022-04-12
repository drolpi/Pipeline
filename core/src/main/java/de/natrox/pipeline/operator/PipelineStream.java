package de.natrox.pipeline.operator;

import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.operator.filter.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PipelineStream<T extends PipelineData> {

    @NotNull
    Optional<T> first();

    @NotNull
    CompletableFuture<Optional<T>> firstAsync();

    @NotNull
    List<T> collect();

    @NotNull
    CompletableFuture<List<T>> collectAsync();

    @NotNull
    PipelineStream<T> filter(@NotNull Filter filter);

    //TEMP
    @Deprecated
    @NotNull
    PipelineStream<T> sort(@NotNull Object sorter);

    @NotNull
    PipelineStream<T> limit(int limit);

    @NotNull
    PipelineStream<T> skip(int skip);

}
