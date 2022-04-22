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

package de.natrox.pipeline;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.document.DocumentCollection;
import de.natrox.pipeline.document.DocumentCollectionFactory;
import de.natrox.pipeline.object.ObjectCollection;
import de.natrox.pipeline.object.ObjectCollectionFactory;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.part.cache.DataUpdater;
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.cache.LocalCache;
import de.natrox.pipeline.part.cache.provider.DataUpdaterProvider;
import de.natrox.pipeline.part.cache.provider.GlobalCacheProvider;
import de.natrox.pipeline.part.cache.provider.LocalCacheProvider;
import de.natrox.pipeline.part.storage.Storage;
import de.natrox.pipeline.part.storage.provider.GlobalStorageProvider;
import de.natrox.pipeline.part.storage.provider.LocalStorageProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

final class PipelineImpl implements Pipeline {

    private final Storage storage;
    private final GlobalCache globalCache;
    private final DataUpdater dataUpdater;
    private final LocalCache localCache;

    private final DocumentCollectionFactory documentCollectionFactory;
    private final ObjectCollectionFactory objectCollectionFactory;

    PipelineImpl(@NotNull PartBundle partBundle) {
        Check.notNull(partBundle, "partBundle");

        // check if a LocalBundle or a GlobalBundle is used
        if (partBundle instanceof LocalBundle localBundle) {
            LocalStorageProvider localStorageProvider = localBundle.localStorageProvider();
            this.storage = localStorageProvider.constructLocalStorage();

            LocalCacheProvider localCacheProvider = localBundle.localCacheProvider();
            // check if there is a local cache provider or initialize null
            if (localCacheProvider != null) {
                this.localCache = localCacheProvider.constructLocalCache();
            } else {
                this.localCache = null;
            }

            this.globalCache = null;
            this.dataUpdater = null;
        } else {
            GlobalBundle globalBundle = (GlobalBundle) partBundle;

            GlobalStorageProvider globalStorageProvider = globalBundle.globalStorageProvider();
            this.storage = globalStorageProvider.constructGlobalStorage();

            GlobalCacheProvider globalCacheProvider = globalBundle.globalCacheProvider();
            // check if there is a global cache provider or initialize null
            if (globalCacheProvider != null) {
                this.globalCache = globalCacheProvider.constructGlobalCache();
            } else {
                this.globalCache = null;
            }

            DataUpdaterProvider dataUpdaterProvider = globalBundle.dataUpdaterProvider();
            // check if there is a data updater provider or initialize null
            if (dataUpdaterProvider != null) {
                this.dataUpdater = dataUpdaterProvider.constructDataUpdater();
            } else {
                this.dataUpdater = null;
            }

            LocalCacheProvider localCacheProvider = globalBundle.localCacheProvider();
            // check if there is a local cache provider or initialize null
            if (localCacheProvider != null) {
                this.localCache = localCacheProvider.constructLocalCache();
            } else {
                this.localCache = null;
            }
        }

        this.documentCollectionFactory = new DocumentCollectionFactory();
        this.objectCollectionFactory = new ObjectCollectionFactory();
    }

    @Override
    public @NotNull DocumentCollection collection(@NotNull String name) {
        Check.notNull(name, "name");
        return documentCollectionFactory.collection(name);
    }

    @Override
    public <T extends ObjectData> @NotNull ObjectCollection<T> collection(@NotNull Class<T> type) {
        return null;
    }

    @Override
    public void destroyCollection(@NotNull String name) {

    }

    @Override
    public <T extends ObjectData> void destroyCollection(@NotNull Class<T> type) {

    }

    @Override
    public @NotNull Set<String> listDocumentCollections() {
        return null;
    }

    @Override
    public @NotNull Set<String> listObjectCollections() {
        return null;
    }

    @Override
    public boolean isShutDowned() {
        return false;
    }

    @Override
    public void shutdown() {

    }
}
