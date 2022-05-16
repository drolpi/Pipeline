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
import de.natrox.pipeline.document.DocumentRepository;
import de.natrox.pipeline.document.DocumentRepositoryFactory;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.object.ObjectRepository;
import de.natrox.pipeline.object.ObjectRepositoryFactory;
import de.natrox.pipeline.object.annotation.AnnotationResolver;
import de.natrox.pipeline.part.connecting.ConnectingStore;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

final class PipelineImpl implements Pipeline {

    private final ConnectingStore connectingStore;
    private final JsonConverter jsonConverter;

    private final DocumentRepositoryFactory documentRepositoryFactory;
    private final ObjectRepositoryFactory objectRepositoryFactory;

    PipelineImpl(@NotNull PartBundle partBundle, @NotNull JsonConverter jsonConverter) {
        Check.notNull(partBundle, "partBundle");
        Check.notNull(jsonConverter, "jsonConverter");

        this.jsonConverter = jsonConverter;
        this.connectingStore = partBundle.createConnectingStore(this);

        this.documentRepositoryFactory = new DocumentRepositoryFactory(this.connectingStore);
        this.objectRepositoryFactory = new ObjectRepositoryFactory(this, this.documentRepositoryFactory);
    }

    @Override
    public @NotNull DocumentRepository repository(@NotNull String name) {
        Check.notNull(name, "name");
        return this.documentRepositoryFactory.repository(name);
    }

    @Override
    public <T extends ObjectData> @NotNull ObjectRepository<T> repository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        return this.objectRepositoryFactory.repository(type);
    }

    @Override
    public void destroyRepository(@NotNull String name) {
        Check.notNull(name, "name");
        this.connectingStore.removeMap(name);
    }

    @Override
    public <T extends ObjectData> void destroyRepository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        this.connectingStore.removeMap(AnnotationResolver.identifier(type));
    }

    @Override
    public @NotNull Set<String> repositories() {
        return this.connectingStore.maps();
    }

    @Override
    public @NotNull JsonConverter jsonConverter() {
        return this.jsonConverter;
    }

    @Override
    public boolean isShutDowned() {
        return this.connectingStore == null || this.connectingStore.isClosed();
    }

    @Override
    public void shutdown() {
        this.connectingStore.close();
        this.documentRepositoryFactory.clear();
    }

    public ConnectingStore store() {
        return this.connectingStore;
    }
}
