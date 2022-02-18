package de.notion.pipeline.datatype;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import de.notion.pipeline.Pipeline;
import de.notion.pipeline.annotation.Context;
import de.notion.pipeline.annotation.PersistentData;
import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.part.PipelineDataSynchronizer;
import de.notion.pipeline.part.local.updater.DataUpdater;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public abstract class PipelineData {

    private final Pipeline pipeline;
    private Gson gson;
    private DataUpdater dataUpdater;
    private long lastUse = System.currentTimeMillis();
    private boolean markedForRemoval = false;

    @PersistentData
    private UUID objectUUID;

    public PipelineData(@NotNull Pipeline pipeline) {
        Objects.requireNonNull(pipeline, "pipeline can't be null!");
        this.pipeline = pipeline;
        this.dataUpdater = pipeline.dataUpdaterService().dataUpdater(getClass());
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }

                    @Override
                    public boolean shouldSkipField(FieldAttributes field) {
                        return field.getAnnotation(PersistentData.class) == null;
                    }
                })
                .registerTypeAdapter(getClass(), (InstanceCreator<PipelineData>) type -> this)
                .create();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PipelineData)) return false;
        PipelineData pipelineData = (PipelineData) o;
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
        save(saveToGlobalStorage, null);
    }

    public void save(boolean saveToGlobalStorage, Runnable callback) {
        long startTime = System.currentTimeMillis();
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
            if (AnnotationResolver.context(getClass()).equals(Context.GLOBAL))
                pipeline.dataSynchronizer().synchronize(PipelineDataSynchronizer.DataSourceType.LOCAL, PipelineDataSynchronizer.DataSourceType.GLOBAL_CACHE, getClass(), objectUUID());

            if (!saveToGlobalStorage)
                return;

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

    public String serialize() {
        unMarkRemoval();
        return gson.toJson(this);
    }

    public PipelineData deserialize(String data) {
        unMarkRemoval();
        return gson.fromJson(data, getClass());
    }
}
