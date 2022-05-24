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

package de.natrox.pipeline.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.object.ObjectData;
import org.jetbrains.annotations.NotNull;

final class GuiceInstanceCreatorImpl<T extends ObjectData> implements GuiceInstanceCreator<T> {

    private final Class<? extends T> type;
    private final Injector injector;
    private final Provider<Pipeline> provider;

    private volatile Pipeline pipeline;

    GuiceInstanceCreatorImpl(Class<? extends T> type, Injector injector) {
        this.type = type;
        this.provider = () -> this.pipeline;
        this.injector = injector.createChildInjector(new PipelineModule());
    }

    @Override
    public @NotNull T create(@NotNull Pipeline pipe) {
        this.pipeline = pipe;
        return this.injector.getInstance(this.type);
    }

    final class PipelineModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(Pipeline.class).toProvider(GuiceInstanceCreatorImpl.this.provider);
        }
    }
}
