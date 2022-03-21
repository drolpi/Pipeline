package de.natrox.pipeline.datatype;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.DataSynchronizer;
import de.natrox.pipeline.part.updater.DataUpdater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public abstract class PipelineData implements DataType {

    private final transient Pipeline pipeline;
    private final transient Gson gson;
    private final transient DataUpdater dataUpdater;
    private transient long lastUse = System.currentTimeMillis();
    private transient boolean markedForRemoval = false;

    private UUID objectUUID;

    public PipelineData(@NotNull Pipeline pipeline) {
        Objects.requireNonNull(pipeline, "pipeline can't be null!");
        this.pipeline = pipeline;
        this.dataUpdater = pipeline.dataUpdaterService().dataUpdater(getClass());
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(getClass(), (InstanceCreator<PipelineData>) type -> this)
            .create();
    }

    @NotNull
    public UUID objectUUID() {
        return objectUUID;
    }

    @Override
    public void save(@Nullable Runnable callback) {
        var startTime = System.currentTimeMillis();
        System.out.println("Saving " + getClass().getSimpleName() + " with uuid " + objectUUID);
        updateLastUse();

        var runnable = new Runnable() {
            private int runCount = 0;

            @Override
            public void run() {
                runCount++;
                if (runCount != 2)
                    return;

                System.out.println("Done saving in " + (System.currentTimeMillis() - startTime) + "ms [" + getClass().getSimpleName() + "]");
                if (callback != null)
                    callback.run();
            }
        };

        this.dataUpdater.pushUpdate(this, () -> {
            pipeline.dataSynchronizer()
                .synchronize(DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_CACHE, getClass(), objectUUID(), runnable, null);

            pipeline.dataSynchronizer()
                .synchronize(DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_STORAGE, getClass(), objectUUID(), runnable, null);
        });
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

    @NotNull
    @Override
    public DataUpdater dataUpdater() {
        return dataUpdater;
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        unMarkRemoval();
        return gson.toJsonTree(this).getAsJsonObject();
    }

    @NotNull
    public String serializeToString() {
        return gson.toJson(serialize());
    }

    @NotNull
    @Override
    public PipelineData deserialize(JsonObject data) {
        unMarkRemoval();
        return gson.fromJson(data, getClass());
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
