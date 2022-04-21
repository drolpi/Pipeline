/*
 * Copyright 2020-2022 NatroxMC team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.natrox.pipeline.operator;

import de.natrox.common.validate.Check;
import de.natrox.common.runnable.CatchingRunnable;
import de.natrox.pipeline.PipelineImpl;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.operator.filter.Filter;
import de.natrox.pipeline.part.DataSynchronizerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public final class PipelineStreamImpl<T extends PipelineData> implements PipelineStream<T> {

    private final PipelineImpl pipeline;
    private final DataSynchronizerImpl dataSynchronizer;
    private final ExecutorService executorService;
    private final Class<? extends T> dataClass;
    private final Consumer<T> callback;
    private final InstanceCreator<T> instanceCreator;
    private final FindOptions findOptions;

    public PipelineStreamImpl(
        @NotNull PipelineImpl pipeline,
        @NotNull DataSynchronizerImpl dataSynchronizer,
        @NotNull Class<? extends T> dataClass,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        this.pipeline = pipeline;
        this.executorService = pipeline.executorService();
        this.dataSynchronizer = dataSynchronizer;
        this.dataClass = dataClass;
        this.callback = callback;
        this.instanceCreator = instanceCreator;
        this.findOptions = new FindOptions();
    }

    @Override
    public @NotNull Optional<T> first() {
        var data = pipeline.entries(dataClass);
        data = applyOptions(data);

        for (var entry : data.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var localData = dataSynchronizer.toLocal(dataClass, key, value, instanceCreator);
            return Optional.ofNullable(localData);
        }
        return Optional.empty();
    }

    @Override
    public @NotNull CompletableFuture<Optional<T>> firstAsync() {
        var completableFuture = new CompletableFuture<Optional<T>>();
        executorService.submit(new CatchingRunnable(() -> completableFuture.complete(first())));
        return completableFuture;
    }

    @Override
    public @NotNull List<T> collect() {
        var data = pipeline.entries(dataClass);
        data = applyOptions(data);

        List<T> values = new ArrayList<>();
        for (var entry : data.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var localData = dataSynchronizer.toLocal(dataClass, key, value, instanceCreator);
            values.add(localData);
        }
        return values;
    }

    @Override
    public @NotNull CompletableFuture<List<T>> collectAsync() {
        var completableFuture = new CompletableFuture<List<T>>();
        executorService.submit(new CatchingRunnable(() -> completableFuture.complete(collect())));
        return completableFuture;
    }

    @Override
    public @NotNull PipelineStream<T> filter(@NotNull Filter filter) {
        Check.notNull(filter, "filter");
        findOptions.setFilter(filter);
        return this;
    }

    @Override
    public @NotNull PipelineStream<T> sort(@NotNull Object sorter) {
        Check.notNull(sorter, "sorter");
        findOptions.setSort(sorter);
        return this;
    }

    @Override
    public @NotNull PipelineStream<T> limit(int limit) {
        findOptions.setLimit(limit);
        return this;
    }

    @Override
    public @NotNull PipelineStream<T> skip(int skip) {
        findOptions.setSkip(skip);
        return this;
    }

    private Map<UUID, JsonDocument> applyOptions(Map<UUID, JsonDocument> data) {
        var newData = new HashMap<UUID, JsonDocument>();

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
