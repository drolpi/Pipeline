package de.natrox.pipeline.operator;

import com.google.gson.JsonObject;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import de.natrox.pipeline.operator.filter.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public final class PipelineStreamImpl<T extends PipelineData> implements PipelineStream<T> {

    //TODO: Was ist wenn Pipeline#globalStorage null zurückgibt, weil die Pipeline ohne global storage erzeugt wurde?

    private final Pipeline pipeline;
    private final Class<? extends T> dataClass;
    private final Pipeline.LoadingStrategy loadingStrategy;
    private final Consumer<T> callback;
    private final InstanceCreator<T> instanceCreator;
    private final FindOptions findOptions;

    public PipelineStreamImpl(
            @NotNull Pipeline pipeline,
            @NotNull Class<? extends T> dataClass,
            @NotNull Pipeline.LoadingStrategy loadingStrategy,
            @Nullable Consumer<T> callback,
            @Nullable InstanceCreator<T> instanceCreator
    ) {
        this.pipeline = pipeline;
        this.dataClass = dataClass;
        this.loadingStrategy = loadingStrategy;
        this.callback = callback;
        this.instanceCreator = instanceCreator;
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
            return pipeline.load(dataClass, uuid, loadingStrategy, callback, instanceCreator);
        }
        return null;
    }

    @NotNull
    @Override
    public List<T> collect() {
        var data = pipeline.globalStorage().data(dataClass);
        data = applyOptions(data);
        return pipeline.load(dataClass, data.keySet(), loadingStrategy, callback, instanceCreator);
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

        if (skip != -1 && limit != -1)
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
