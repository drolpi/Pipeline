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

import java.util.Objects;
import java.util.UUID;

public abstract class PipelineData implements DataType {

    private final static Logger LOGGER = LoggerFactory.getLogger(PipelineData.class);

    private final transient Pipeline pipeline;
    private final transient PipelineDataSerializer serializer;
    private final transient DataUpdater dataUpdater;
    private transient long lastUse = System.currentTimeMillis();
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
        var startTime = System.currentTimeMillis();
        LOGGER.debug("Saving " + getClass().getSimpleName() + " with uuid " + objectUUID);
        updateLastUse();

        var runnable = new Runnable() {
            private int runCount = 0;

            @Override
            public void run() {
                runCount++;
                if (runCount != 2)
                    return;

                LOGGER.debug("Done saving in " + (System.currentTimeMillis() - startTime) + "ms [" + getClass().getSimpleName() + "]");
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
    public long lastUse() {
        return lastUse;
    }

    @Override
    public void updateLastUse() {
        this.lastUse = System.currentTimeMillis();
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
