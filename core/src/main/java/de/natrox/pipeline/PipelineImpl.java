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

import de.natrox.pipeline.document.DocumentCollection;
import de.natrox.pipeline.document.DocumentCollectionFactory;
import de.natrox.pipeline.object.ObjectCollection;
import de.natrox.pipeline.object.ObjectCollectionFactory;

import java.util.Set;

final class PipelineImpl implements Pipeline {

    private final DocumentCollectionFactory documentCollectionFactory;
    private final ObjectCollectionFactory objectCollectionFactory;

    PipelineImpl() {
        this.documentCollectionFactory = new DocumentCollectionFactory();
        this.objectCollectionFactory = new ObjectCollectionFactory();
    }

    @Override
    public DocumentCollection collection(String name) {
        return null;
    }

    @Override
    public ObjectCollection collection(Class<?> type) {
        return null;
    }

    @Override
    public void destroyCollection(String name) {

    }

    @Override
    public void destroyCollection(Class<?> type) {

    }

    @Override
    public Set<String> listDocumentCollections() {
        return null;
    }

    @Override
    public Set<String> listObjectCollections() {
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
