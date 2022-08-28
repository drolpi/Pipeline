/*
 * Copyright 2020-2022 NatroxMC
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

package de.natrox.pipeline.repository;

import de.natrox.common.container.Pair;
import de.natrox.common.validate.Check;
import de.natrox.eventbus.EventBus;
import de.natrox.eventbus.EventListener;
import de.natrox.pipeline.concurrent.LockService;
import de.natrox.pipeline.condition.Condition;
import de.natrox.pipeline.exception.NodeSerializeException;
import de.natrox.pipeline.find.FindOptions;
import de.natrox.pipeline.part.updater.Updater;
import de.natrox.pipeline.part.updater.event.EntryUpdateEvent;
import de.natrox.pipeline.part.updater.event.NodeUpdateEvent;
import de.natrox.pipeline.serializer.NodeSerializer;
import de.natrox.pipeline.sort.SortEntry;
import de.natrox.pipeline.sort.SortOrder;
import de.natrox.pipeline.stream.BoundedStream;
import de.natrox.pipeline.stream.ConditionalStream;
import de.natrox.pipeline.stream.DataNodeStream;
import de.natrox.pipeline.stream.DataStream;
import de.natrox.pipeline.stream.SortedStream;
import de.natrox.pipeline.node.DataNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
final class RepositoryImpl implements Repository {

    private final String repositoryName;
    private final RepositoryOptions options;
    private final NodeSerializer nodeSerializer;

    private final Lock writeLock;
    private final Lock readLock;

    private PipelineStore pipelineStore;
    private PipelineMap pipelineMap;

    RepositoryImpl(String repositoryName, PipelineStore pipelineStore, PipelineMap pipelineMap, LockService lockService, RepositoryOptions options) {
        this.repositoryName = repositoryName;
        this.pipelineStore = pipelineStore;
        this.pipelineMap = pipelineMap;
        this.options = options;
        this.nodeSerializer = options.nodeSerializer();
        this.readLock = lockService.getReadLock(repositoryName);
        this.writeLock = lockService.getWriteLock(repositoryName);
        this.registerListeners();
    }

    @Override
    public @NotNull Optional<DataNode> load(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");

        try {
            this.readLock.lock();
            this.checkOpened();

            Object serializedData = this.pipelineMap.get(uniqueId);
            if (serializedData == null)
                return Optional.empty();

            DataNode node = this.readData(serializedData);

            return Optional.of(node);
        } finally {
            this.readLock.unlock();
        }
    }

    @Override
    public @NotNull DataNode loadOrCreate(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        return this.load(uniqueId).orElseGet(() -> this.create(uniqueId));
    }

    private DataNode create(UUID uniqueId, QueryStrategy @NotNull ... strategies) {
        Check.notNull(uniqueId, "uniqueId");
        try {
            this.writeLock.lock();
            this.checkOpened();

            DataNode node = null;
            Object serializedData = this.nodeSerializer.write(node);

            this.pipelineMap.put(uniqueId, serializedData, strategies);
            return node;
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public @NotNull DataNodeStream find(@NotNull FindOptions findOptions) {
        Check.notNull(findOptions, "findOptions");

        try {
            this.readLock.lock();
            this.checkOpened();
            DataStream<Pair<UUID, DataNode>> stream = DataStream.fromIterable(
                this.pipelineMap
                    .entries()
                    .entrySet()
                    .stream()
                    .map(entry -> Pair.of(entry.getKey(), this.readData(entry.getValue())))
                    .collect(Collectors.toList())
            );

            Condition condition = findOptions.condition();
            if (condition != null) {
                stream = new ConditionalStream(condition, stream);
            }

            SortEntry sortBy = findOptions.sortBy();
            if (sortBy != null) {
                List<Pair<Object[], SortOrder>> blockingSortOrder = sortBy.sortingOrders();
                if (!blockingSortOrder.isEmpty()) {
                    stream = new SortedStream(blockingSortOrder, stream);
                }
            }

            if (findOptions.limit() != -1 || findOptions.skip() != -1) {
                long limit = findOptions.limit() == -1 ? Long.MAX_VALUE : findOptions.limit();
                long skip = findOptions.skip() == -1 ? 0 : findOptions.skip();
                stream = new BoundedStream<>(skip, limit, stream);
            }

            return new DataNodeStream(stream);
        } finally {
            this.readLock.unlock();
        }
    }

    @Override
    public boolean exists(@NotNull UUID uniqueId, QueryStrategy @NotNull ... strategies) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(strategies, "strategies");
        Check.argCondition(strategies.length <= 0, "strategies");
        try {
            this.readLock.lock();
            this.checkOpened();
            return this.pipelineMap.contains(uniqueId, strategies);
        } finally {
            this.readLock.unlock();
        }
    }

    @Override
    public void remove(@NotNull UUID uniqueId, QueryStrategy @NotNull ... strategies) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(strategies, "strategies");
        Check.argCondition(strategies.length <= 0, "strategies");
        try {
            this.writeLock.lock();
            this.checkOpened();
            this.pipelineMap.remove(uniqueId, strategies);
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public void save(@NotNull UUID uniqueId, @NotNull DataNode objectData, QueryStrategy @NotNull ... strategies) {

    }

    @Override
    public @NotNull String name() {
        return this.repositoryName;
    }

    @Override
    public void clear() {
        try {
            this.writeLock.lock();
            this.checkOpened();
            this.pipelineMap.clear();
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public void close() {
        try {
            this.writeLock.lock();
            if (this.pipelineStore == null || this.pipelineMap == null) {
                return;
            }

            this.pipelineStore.closeMap(this.repositoryName);
            this.pipelineMap.close();

            this.pipelineStore = null;
            this.pipelineMap = null;
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public boolean isDropped() {
        return !this.pipelineStore.hasMap(this.repositoryName);
    }

    @Override
    public void drop() {
        try {
            this.writeLock.lock();
            this.checkOpened();

            this.pipelineStore.removeMap(this.repositoryName);
            this.pipelineStore.closeMap(this.repositoryName);

            this.pipelineStore = null;
            this.pipelineMap = null;
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public boolean isOpen() {
        try {
            this.readLock.lock();
            return this.pipelineStore != null && !this.pipelineStore.isClosed() && !this.isDropped();
        } finally {
            this.readLock.unlock();
        }
    }

    @Override
    public long size() {
        try {
            this.readLock.lock();
            this.checkOpened();
            return this.pipelineMap.size();
        } finally {
            this.readLock.unlock();
        }
    }

    private void checkOpened() {
        if (this.isOpen())
            return;
        throw new RuntimeException("Repository is closed");
    }

    private DataNode readData(Object data) {
        try {
            return this.nodeSerializer.read(data);
        } catch (NodeSerializeException e) {
            //TODO: message/reason
            throw new RuntimeException(e);
        }
    }

    private void registerListeners() {
        Updater updater = this.pipelineStore.updater();

        if (updater == null)
            return;

        EventBus eventBus = updater.eventBus();

        eventBus.register(
            EventListener
                .builder(EntryUpdateEvent.class)
                .condition(event -> event.repositoryName().equals(this.repositoryName))
                .handler(event -> eventBus.call(new NodeUpdateEvent(
                    event.senderId(),
                    event.repositoryName(),
                    event.dataKey(),
                    this.readData(event.data())))
                )
                .build()
        );
    }
}
