package de.notion.pipeline.operator;

import com.google.gson.JsonObject;
import de.notion.pipeline.Pipeline;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.operator.filter.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
        var data = pipeline.globalStorage().data(dataClass);
        System.out.println(data.size());
        data = applyOptions(data);

        System.out.println(data.size());

        for (UUID uuid : data.keySet()) {
            return pipeline.load(dataClass, uuid, loadingStrategy);
        }
        return null;
    }

    @NotNull
    @Override
    public List<T> collect() {
        var data = pipeline.globalStorage().data(dataClass);
        data = applyOptions(data);
        return pipeline.load(dataClass, data.keySet(), loadingStrategy);
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

    private Map<UUID, JsonObject> applyOptions(Map<UUID, JsonObject> data) {
        var newData = new HashMap<UUID, JsonObject>();

        var filter = findOptions.filter();
        var skip = findOptions.skip();
        var limit = findOptions.limit();

        if(skip != -1 && limit != -1)
            limit = limit + skip;

        var i = -1;
        for (var entry : data.entrySet()) {
            i++;
            if (skip > i && skip != -1)
                continue;
            if (i > limit && limit != -1)
                break;

            if (filter != null && !filter.check(entry.getValue()))
                continue;

            newData.put(entry.getKey(), entry.getValue());
        }

        return newData;
    }
}
