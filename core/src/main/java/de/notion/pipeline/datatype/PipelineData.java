package de.notion.pipeline.datatype;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import de.notion.pipeline.Pipeline;
import de.notion.pipeline.part.PipelineDataSynchronizer;
import de.notion.pipeline.part.local.updater.DataUpdater;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public abstract class PipelineData {

    private final transient Pipeline pipeline;
    private transient Gson gson;
    private transient DataUpdater dataUpdater;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PipelineData))
            return false;

        var pipelineData = (PipelineData) o;
        return Objects.equals(objectUUID(), pipelineData.objectUUID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectUUID());
    }

    public UUID objectUUID() {
        return objectUUID;
    }

    public void save(boolean saveToGlobalStorage) {
        save(true, saveToGlobalStorage, null);
    }

    public void save(boolean saveToGlobalCache, boolean saveToGlobalStorage, Runnable callback) {
        var startTime = System.currentTimeMillis();
        System.out.println("Saving " + getClass().getSimpleName() + " with uuid " + objectUUID);
        updateLastUse();

        Runnable internal = () -> {
            System.out.println("Done saving in " + (System.currentTimeMillis() - startTime) + "ms [" + getClass().getSimpleName() + "]");
            if (callback != null)
                callback.run();
        };

        if (this.dataUpdater == null)
            return;
        this.dataUpdater.pushUpdate(this, () -> {
            if (saveToGlobalCache)
                pipeline.dataSynchronizer().synchronize(PipelineDataSynchronizer.DataSourceType.LOCAL, PipelineDataSynchronizer.DataSourceType.GLOBAL_CACHE, getClass(), objectUUID());

            if (saveToGlobalStorage)
                pipeline.dataSynchronizer().synchronize(PipelineDataSynchronizer.DataSourceType.LOCAL, PipelineDataSynchronizer.DataSourceType.GLOBAL_STORAGE, getClass(), objectUUID(), internal);
        });
    }

    /**
     * Executed after a DataManipulator synced the object
     *
     * @param dataBeforeSync The data the object had before syncing
     */
    public void onSync(PipelineData dataBeforeSync) {
    }

    /**
     * Executed after instantiation of the Object
     * Executed before Object is put into LocalCache
     */
    public void onCreate() {
    }

    /**
     * Executed before the object is deleted from local cache.
     */
    public void onDelete() {
    }

    /**
     * Executed directly after Data was loaded from Pipeline. Not if it was found in LocalCache
     */
    public void onLoad() {
    }

    /**
     * Executed before onLoad and before onCreate everytime the data is being loaded into local cache.
     * You can use this function to load dependent data from pipeline that is directly associated with this data
     */
    public void loadDependentData() {
    }

    /**
     * Executed before Data is cleared from LocalCache
     */
    public void onCleanUp() {
    }

    public void markForRemoval() {
        this.markedForRemoval = true;
    }

    public void unMarkRemoval() {
        this.markedForRemoval = false;
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    public void updateLastUse() {
        this.lastUse = System.currentTimeMillis();
        if (pipeline.globalCache() != null)
            pipeline.globalCache().updateExpireTime(getClass(), objectUUID());
    }

    public long lastUse() {
        return lastUse;
    }

    public DataUpdater dataUpdater() {
        return dataUpdater;
    }

    public Pipeline pipeline() {
        return pipeline;
    }

    public JsonObject serialize() {
        unMarkRemoval();
        return gson.toJsonTree(this).getAsJsonObject();
    }

    public String serializeToString() {
        return gson.toJson(serialize());
    }

    public PipelineData deserialize(JsonObject data) {
        unMarkRemoval();
        return gson.fromJson(data, getClass());
    }
}
