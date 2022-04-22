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
import de.natrox.common.validate.Check;
import de.natrox.pipeline.document.DocumentCollection;
import de.natrox.pipeline.object.ObjectCollection;
import de.natrox.pipeline.object.ObjectData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@ApiStatus.Experimental
public interface Pipeline {

    static @NotNull Builder builder() {
        return new PipelineBuilderImpl();
    }

    @NotNull DocumentCollection collection(@NotNull String name);

    <T extends ObjectData> @NotNull ObjectCollection<T> collection(@NotNull Class<T> type);

    void destroyCollection(@NotNull String name);

    <T extends ObjectData> void destroyCollection(@NotNull Class<T> type);

    @NotNull Set<String> listDocumentCollections();

    @NotNull Set<String> listObjectCollections();

    boolean isShutDowned();

    void shutdown();

    default boolean hasCollection(@NotNull String name) {
        Check.notNull(name, "name");
        return this.listDocumentCollections().contains(name);
    }

    default <T> boolean hasCollection(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        return this.listObjectCollections().contains(type.getName());
    }

    interface Builder extends IBuilder<Pipeline> {

    }

}
