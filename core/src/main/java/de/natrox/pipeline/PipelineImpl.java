/*
 * Copyright 2020-2022 NatroxMC
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
import de.natrox.pipeline.document.option.DocumentOptions;
import de.natrox.pipeline.mapper.Mapper;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.object.ObjectRepository;
import de.natrox.pipeline.object.ObjectRepositoryFactory;
import de.natrox.pipeline.object.annotation.AnnotationResolver;
import de.natrox.pipeline.object.option.ObjectOptions;
import de.natrox.pipeline.part.connecting.ConnectingStore;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

final class PipelineImpl implements Pipeline {

    private final ConnectingStore connectingStore;
    private final Mapper mapper;

    private final DocumentRepositoryFactory documentRepositoryFactory;
    private final ObjectRepositoryFactory objectRepositoryFactory;

    PipelineImpl(@NotNull PartBundle partBundle, @NotNull Mapper mapper) {
        Check.notNull(partBundle, "partBundle");
        Check.notNull(mapper, "mapper");

        this.mapper = mapper;
        this.connectingStore = partBundle.createConnectingStore(this);

        this.documentRepositoryFactory = new DocumentRepositoryFactory(this.connectingStore);
        this.objectRepositoryFactory = new ObjectRepositoryFactory(this, this.documentRepositoryFactory);
    }

    @Override
    public @NotNull DocumentRepository repository(@NotNull String name, @NotNull DocumentOptions options) {
        Check.notNull(name, "name");
        Check.notNull(options, "options");
        return this.documentRepositoryFactory.repository(name, options);
    }

    @Override
    public <T extends ObjectData> @NotNull ObjectRepository<T> repository(@NotNull Class<T> type, @NotNull ObjectOptions<T> options) {
        Check.notNull(type, "type");
        Check.notNull(options, "options");
        return this.objectRepositoryFactory.repository(type, options);
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
    public @NotNull Mapper mapper() {
        return this.mapper;
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
