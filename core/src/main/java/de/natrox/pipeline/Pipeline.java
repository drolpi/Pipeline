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

import de.natrox.common.builder.IBuilder;
import de.natrox.pipeline.document.DocumentCollection;
import de.natrox.pipeline.object.ObjectCollection;

import java.util.Set;

public interface Pipeline {

    static Builder builder() {
        return new PipelineBuilderImpl();
    }

    DocumentCollection collection(String name);

    ObjectCollection collection(Class<?> type);

    void destroyCollection(String name);

    void destroyCollection(Class<?> type);

    Set<String> listDocumentCollections();

    Set<String> listObjectCollections();

    boolean isShutDowned();

    void shutdown();

    default boolean hasCollection(String name) {
        return this.listDocumentCollections().contains(name);
    }

    default boolean hasRepository(Class<?> type) {
        return this.listObjectCollections().contains(type.getName());
    }

    interface Builder extends IBuilder<Pipeline> {

    }

}
