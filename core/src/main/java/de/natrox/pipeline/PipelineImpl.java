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
import de.natrox.pipeline.concurrent.LockService;
import de.natrox.pipeline.document.DocumentRepository;
import de.natrox.pipeline.document.DocumentRepositoryFactory;
import de.natrox.pipeline.document.option.DocumentOptions;
import de.natrox.pipeline.exception.PipelineException;
import de.natrox.pipeline.mapper.DocumentMapper;
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
    private final DocumentMapper documentMapper;

    private final DocumentRepositoryFactory documentRepositoryFactory;
    private final ObjectRepositoryFactory objectRepositoryFactory;

    PipelineImpl(@NotNull PipelineBuilderImpl.AbstractBuilder<?> builder) {
        Check.notNull(builder, "builder");

        this.documentMapper = DocumentMapper.create();
        LockService lockService = new LockService();
        this.connectingStore = builder.createConnectingStore(this);

        this.documentRepositoryFactory = new DocumentRepositoryFactory(this, this.connectingStore, lockService);
        this.objectRepositoryFactory = new ObjectRepositoryFactory(this, this.connectingStore, this.documentRepositoryFactory);
    }

    @Override
    public @NotNull DocumentRepository repository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        return this.documentRepositoryFactory.repository(name);
    }

    @Override
    public @NotNull DocumentRepository createRepository(@NotNull String name, @NotNull DocumentOptions options) {
        Check.notNull(name, "name");
        Check.notNull(options, "options");
        this.checkOpened();
        return this.documentRepositoryFactory.createRepository(name, options);
    }

    @Override
    public @NotNull <T extends ObjectData> ObjectRepository<T> repository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        this.checkOpened();
        return this.objectRepositoryFactory.repository(type);
    }

    @Override
    public <T extends ObjectData> @NotNull ObjectRepository<T> createRepository(@NotNull Class<T> type, @NotNull ObjectOptions<T> options) {
        Check.notNull(type, "type");
        Check.notNull(options, "options");
        this.checkOpened();
        return this.objectRepositoryFactory.createRepository(type, options);
    }

    @Override
    public boolean hasRepository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        return this.repositories().contains(name);
    }

    @Override
    public <T> boolean hasRepository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        this.checkOpened();
        return this.repositories().contains(AnnotationResolver.identifier(type));
    }

    @Override
    public void destroyRepository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        this.connectingStore.removeMap(name);
    }

    @Override
    public <T extends ObjectData> void destroyRepository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        this.checkOpened();
        this.connectingStore.removeMap(AnnotationResolver.identifier(type));
    }

    @Override
    public @NotNull Set<String> repositories() {
        this.checkOpened();
        return this.connectingStore.maps();
    }

    @Override
    public @NotNull DocumentMapper documentMapper() {
        return this.documentMapper;
    }

    @Override
    public boolean isClosed() {
        return this.connectingStore == null || this.connectingStore.isClosed();
    }

    @Override
    public void close() {
        this.connectingStore.close();
        this.documentRepositoryFactory.clear();
        this.objectRepositoryFactory.clear();
    }

    public void checkOpened() {
        if (!this.isClosed())
            return;
        throw new PipelineException("Pipeline is closed");
    }
}
