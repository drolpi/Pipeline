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

import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.part.updater.DataUpdater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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

    @NotNull UUID objectUUID();

    default void save() {
        save(null);
    }

    void save(@Nullable Runnable callback);

    boolean isMarkedForRemoval();

    void markForRemoval();

    void unMarkRemoval();

    long lastUse();

    void updateLastUse();

    @NotNull DataUpdater dataUpdater();

    @NotNull JsonDocument serialize();

    @NotNull DataType deserialize(@NotNull JsonDocument jsonObject);

}
