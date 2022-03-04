package de.notion.pipeline.datatype;

import com.google.gson.JsonObject;

public interface DataType {

    /**
     * Executed after a DataManipulator synced the object
     *
     * @param dataBeforeSync The data the object had before syncing
     */
    default void onSync(DataType dataBeforeSync) {
    }

    /**
     * Executed after instantiation of the Object
     * Executed before Object is put into LocalCache
     */
    default void onCreate() {
    }

    /**
     * Executed before the object is deleted from local cache.
     */
    default void onDelete() {
    }

    /**
     * Executed directly after Data was loaded from Pipeline. Not if it was found in LocalCache
     */
    default void onLoad() {

    }

    /**
     * Executed before onLoad and before onCreate everytime the data is being loaded into local cache.
     * You can use this function to load dependent data from pipeline that is directly associated with this data
     */
    default void loadDependentData() {

    }

    /**
     * Executed before Data is cleared from LocalCache
     */
    default void onCleanUp() {
    }

    JsonObject serialize();

    /**
     * @param jsonObject New Data to deserialize
     * @return The Data before deserialization
     */
    DataType deserialize(JsonObject jsonObject);

}
