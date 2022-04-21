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

package de.natrox.pipeline.datatype;

import com.google.common.base.Preconditions;
import de.natrox.common.runnable.CatchingRunnable;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.json.serializer.PipelineDataSerializer;
import de.natrox.pipeline.part.DataSynchronizer;
import de.natrox.pipeline.part.updater.DataUpdater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PipelineData implements DataType {

    private final static Logger LOGGER = LoggerFactory.getLogger(PipelineData.class);

    private final transient Pipeline pipeline;
    private final transient PipelineDataSerializer serializer;
    private final transient DataUpdater dataUpdater;
    private transient Instant lastUse = Instant.now();
    private transient boolean markedForRemoval = false;

    @SuppressWarnings("unused")
    private UUID objectUUID;

    public PipelineData(@NotNull Pipeline pipeline) {
        Preconditions.checkNotNull(pipeline, "pipeline");
        this.pipeline = pipeline;
        this.dataUpdater = pipeline.dataUpdater();
        this.serializer = pipeline.serializerFactory().create(this);
    }

    public @NotNull UUID objectUUID() {
        return objectUUID;
    }

    @Override
    public void save(@Nullable Runnable callback) {
        var startInstant = Instant.now();
        LOGGER.debug("Saving " + getClass().getSimpleName() + " with uuid " + objectUUID);
        updateLastUse();

        var shouldRunCount = new AtomicInteger();
        if (pipeline.globalCache() != null)
            shouldRunCount.getAndIncrement();
        if (pipeline.globalStorage() != null)
            shouldRunCount.getAndIncrement();

        var runnable = new Runnable() {
            private int runCount = 0;

            @Override
            public void run() {
                runCount++;
                if (runCount != shouldRunCount.get())
                    return;

                LOGGER.debug("Done saving in " + Duration.between(startInstant, Instant.now()).toMillis() + "ms [" + getClass().getSimpleName() + "]");
                if (callback != null)
                    callback.run();
            }
        };

        this.dataUpdater.pushUpdate(this, new CatchingRunnable(() -> {
            pipeline.dataSynchronizer()
                .synchronize(DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_CACHE, getClass(), objectUUID(), runnable, null);

            pipeline.dataSynchronizer()
                .synchronize(DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_STORAGE, getClass(), objectUUID(), runnable, null);
        }));
    }

    @Override
    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    @Override
    public void markForRemoval() {
        this.markedForRemoval = true;
    }

    @Override
    public void unMarkRemoval() {
        this.markedForRemoval = false;
    }

    @Override
    public Instant lastUse() {
        return lastUse;
    }

    @Override
    public void updateLastUse() {
        this.lastUse = Instant.now();
        var globalCache = pipeline.globalCache();

        if (globalCache != null)
            globalCache.updateExpireTime(getClass(), objectUUID());
    }

    @Override
    public @NotNull DataUpdater dataUpdater() {
        return dataUpdater;
    }

    @Override
    public @NotNull JsonDocument serialize() {
        unMarkRemoval();
        return pipeline.documentFactory().newDocument(this);
    }

    @Override
    public @NotNull PipelineData deserialize(@NotNull JsonDocument data) {
        Preconditions.checkNotNull(data, "jsonObject");
        unMarkRemoval();
        return serializer.toPipelineData(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PipelineData pipelineData))
            return false;

        return Objects.equals(objectUUID(), pipelineData.objectUUID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectUUID());
    }
}
