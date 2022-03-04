package de.notion.pipeline.datatype;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import de.notion.pipeline.Pipeline;
import de.notion.pipeline.part.PipelineDataSynchronizer;
import de.notion.pipeline.part.local.updater.DataUpdater;
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
    public void save(boolean saveToGlobalStorage) {
        save(true, saveToGlobalStorage);
    }

    @Override
    public void save(boolean saveToGlobalCache, boolean saveToGlobalStorage) {
        save(saveToGlobalCache, saveToGlobalStorage, null);
    }

    @Override
    public void save(boolean saveToGlobalCache, boolean saveToGlobalStorage, @Nullable Runnable callback) {
        var startTime = System.currentTimeMillis();
        System.out.println("Saving " + getClass().getSimpleName() + " with uuid " + objectUUID);
        updateLastUse();

        if (this.dataUpdater == null)
            return;
        this.dataUpdater.pushUpdate(this, () -> {
            if (saveToGlobalCache)
                pipeline.dataSynchronizer().synchronize(PipelineDataSynchronizer.DataSourceType.LOCAL, PipelineDataSynchronizer.DataSourceType.GLOBAL_CACHE, getClass(), objectUUID());

            if (saveToGlobalStorage)
                pipeline.dataSynchronizer().synchronize(PipelineDataSynchronizer.DataSourceType.LOCAL, PipelineDataSynchronizer.DataSourceType.GLOBAL_STORAGE, getClass(), objectUUID(), () -> {
                    System.out.println("Done saving in " + (System.currentTimeMillis() - startTime) + "ms [" + getClass().getSimpleName() + "]");
                    if (callback != null)
                        callback.run();
                });
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
        if (pipeline.globalCache() != null)
            pipeline.globalCache().updateExpireTime(getClass(), objectUUID());
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
