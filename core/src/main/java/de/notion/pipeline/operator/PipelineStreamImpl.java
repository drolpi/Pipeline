package de.notion.pipeline.operator;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.operator.filter.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PipelineStreamImpl<T extends PipelineData> implements PipelineStream<T> {

    private final Pipeline pipeline;
    private final Class<? extends T> dataClass;
    private final Pipeline.LoadingStrategy loadingStrategy;
    private final FindOptions findOptions;

    public PipelineStreamImpl(Pipeline pipeline, Class<? extends T> dataClass, Pipeline.LoadingStrategy loadingStrategy) {
        this.pipeline = pipeline;
        this.dataClass = dataClass;
        this.loadingStrategy = loadingStrategy;
        this.findOptions = new FindOptions();
    }

    @Nullable
    @Override
    public T first() {
        var uuids = pipeline.globalStorage().findUUIDs(dataClass, findOptions);
        for (UUID uuid : uuids) {
            return pipeline.load(dataClass, uuid, loadingStrategy);
        }
        return null;
    }

    @NotNull
    @Override
    public List<T> collect() {
        var uuids = pipeline.globalStorage().findUUIDs(dataClass, findOptions);
        return pipeline.loadAllData(dataClass, uuids, loadingStrategy);
    }

    @NotNull
    @Override
    public PipelineStream<T> filter(@NotNull Filter filter) {
        Objects.requireNonNull(filter, "Filter can't be null");
        findOptions.setFilter(filter);
        return this;
    }

    @NotNull
    @Override
    public PipelineStream<T> sort(@NotNull Object sorter) {
        Objects.requireNonNull(sorter, "Sorter can't be null");
        findOptions.setSort(sorter);
        return this;
    }

    @NotNull
    @Override
    public PipelineStream<T> limit(int limit) {
        findOptions.setLimit(limit);
        return this;
    }

    @NotNull
    @Override
    public PipelineStream<T> skip(int skip) {
        findOptions.setSkip(skip);
        return this;
    }
}
