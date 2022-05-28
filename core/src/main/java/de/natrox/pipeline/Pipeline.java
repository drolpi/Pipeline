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

import de.natrox.common.builder.IBuilder;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.document.DocumentRepository;
import de.natrox.pipeline.document.option.DocumentOptions;
import de.natrox.pipeline.mapper.DocumentMapper;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.object.ObjectRepository;
import de.natrox.pipeline.object.option.ObjectOptions;
import de.natrox.pipeline.part.provider.GlobalCacheProvider;
import de.natrox.pipeline.part.provider.GlobalStorageProvider;
import de.natrox.pipeline.part.provider.LocalCacheProvider;
import de.natrox.pipeline.part.provider.LocalStorageProvider;
import de.natrox.pipeline.part.provider.UpdaterProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Consumer;

@ApiStatus.Experimental
public sealed interface Pipeline permits PipelineImpl {

    static @NotNull Builder of(
        @NotNull GlobalStorageProvider globalStorageProvider,
        @NotNull GlobalCacheProvider globalCacheProvider,
        @NotNull LocalCacheProvider localCacheProvider,
        @NotNull UpdaterProvider updaterProvider
    ) {
        return new PipelineBuilderImpl(new PartBundle.Global(
            globalStorageProvider,
            globalCacheProvider,
            localCacheProvider,
            updaterProvider
        ));
    }

    static @NotNull Builder of(
        @NotNull GlobalStorageProvider globalStorageProvider,
        @NotNull LocalCacheProvider localCacheProvider,
        @NotNull UpdaterProvider updaterProvider
    ) {
        return new PipelineBuilderImpl(new PartBundle.Global(
            globalStorageProvider,
            null,
            localCacheProvider,
            updaterProvider
        ));
    }

    static @NotNull Builder of(
        @NotNull GlobalStorageProvider globalStorageProvider,
        @NotNull GlobalCacheProvider globalCacheProvider,
        @NotNull UpdaterProvider updaterProvider
    ) {
        return new PipelineBuilderImpl(new PartBundle.Global(
            globalStorageProvider,
            globalCacheProvider,
            null,
            updaterProvider
        ));
    }

    static @NotNull Builder of(
        @NotNull GlobalStorageProvider globalStorageProvider,
        @NotNull GlobalCacheProvider globalCacheProvider
    ) {
        return new PipelineBuilderImpl(new PartBundle.Global(
            globalStorageProvider,
            globalCacheProvider,
            null,
            null
        ));
    }

    static @NotNull Builder of(@NotNull GlobalStorageProvider globalStorageProvider, @NotNull UpdaterProvider updaterProvider) {
        return new PipelineBuilderImpl(new PartBundle.Global(
            globalStorageProvider,
            null,
            null,
            updaterProvider
        ));
    }

    static @NotNull Builder of(@NotNull GlobalStorageProvider globalStorageProvider) {
        return new PipelineBuilderImpl(new PartBundle.Global(
            globalStorageProvider,
            null,
            null,
            null
        ));
    }

    static @NotNull Builder of(
        @NotNull LocalStorageProvider localStorageProvider,
        @NotNull LocalCacheProvider localCacheProvider
    ) {
        Check.notNull(localStorageProvider, "localStorageProvider");
        Check.notNull(localCacheProvider, "localCacheProvider");
        return new PipelineBuilderImpl(new PartBundle.Local(localStorageProvider, localCacheProvider));
    }

    static @NotNull Builder of(@NotNull LocalStorageProvider localStorageProvider) {
        Check.notNull(localStorageProvider, "localStorageProvider");
        return new PipelineBuilderImpl(new PartBundle.Local(localStorageProvider, null));
    }

    @NotNull DocumentRepository repository(@NotNull String name, @NotNull DocumentOptions options);

    default @NotNull DocumentRepository repository(@NotNull String name, @NotNull Consumer<DocumentOptions.Builder> consumer) {
        DocumentOptions.Builder builder = DocumentOptions.builder();
        consumer.accept(builder);
        return this.repository(name, builder.build());
    }

    default @NotNull DocumentRepository repository(@NotNull String name) {
        return this.repository(name, DocumentOptions.DEFAULT);
    }

    <T extends ObjectData> @NotNull ObjectRepository<T> repository(@NotNull Class<T> type, @NotNull ObjectOptions<T> options);

    default <T extends ObjectData> @NotNull ObjectRepository<T> repository(@NotNull Class<T> type, @NotNull Consumer<ObjectOptions.Builder<T>> consumer) {
        ObjectOptions.Builder<T> builder = ObjectOptions.of(type);
        consumer.accept(builder);
        return this.repository(type, builder.build());
    }

    default <T extends ObjectData> @NotNull ObjectRepository<T> repository(@NotNull Class<T> type) {
        return this.repository(type, ObjectOptions.of(type).build());
    }

    boolean hasRepository(@NotNull String name);

    <T> boolean hasRepository(@NotNull Class<T> type);

    void destroyRepository(@NotNull String name);

    <T extends ObjectData> void destroyRepository(@NotNull Class<T> type);

    @NotNull Set<String> repositories();

    @NotNull DocumentMapper documentMapper();

    boolean isClosed();

    void close();

    sealed interface Builder extends IBuilder<Pipeline> permits PipelineBuilderImpl {

    }

}
