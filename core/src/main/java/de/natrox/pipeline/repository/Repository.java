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

package de.natrox.pipeline.repository;

import de.natrox.common.builder.IBuilder;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.find.FindOptions;
import de.natrox.pipeline.part.config.GlobalCacheConfig;
import de.natrox.pipeline.part.config.LocalCacheConfig;
import de.natrox.pipeline.serializer.NodeSerializer;
import de.natrox.pipeline.stream.Cursor;
import de.natrox.pipeline.node.DataNode;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

@ApiStatus.Experimental
public interface Repository {

    @NotNull Optional<DataNode> load(@NotNull UUID uniqueId);

    @NotNull DataNode loadOrCreate(@NotNull UUID uniqueId);

    @NotNull Cursor<DataNode> find(@NotNull FindOptions findOptions);

    default @NotNull Cursor<DataNode> find(@NotNull UnaryOperator<FindOptions.@NotNull Builder> function) {
        Check.notNull(function, "function");
        return this.find(function.apply(FindOptions.builder()).build());
    }

    default Cursor<DataNode> find() {
        return this.find(FindOptions.defaults());
    }

    boolean exists(@NotNull UUID uniqueId, QueryStrategy @NotNull ... strategies);

    void remove(@NotNull UUID uniqueId, QueryStrategy @NotNull ... strategies);

    void save(@NotNull UUID uniqueId, @NotNull DataNode objectData, QueryStrategy @NotNull ... strategies);

    @NotNull String name();

    void clear();

    void drop();

    boolean isDropped();

    void close();

    boolean isOpen();

    long size();

    sealed interface Builder extends IBuilder<Repository> permits RepositoryBuilder {

        @NotNull Builder withGlobalCache(@NotNull GlobalCacheConfig config);

        default @NotNull Builder withGlobalCache(@NotNull GlobalCacheConfig.Builder builder) {
            Check.notNull(builder, "builder");
            return this.withGlobalCache(builder.build());
        }

        default @NotNull Builder withGlobalCache(@NotNull UnaryOperator<GlobalCacheConfig.Builder> function) {
            Check.notNull(function, "function");
            return this.withGlobalCache(function.apply(GlobalCacheConfig.builder()));
        }

        default @NotNull Builder withGlobalCache() {
            return this.withGlobalCache(GlobalCacheConfig.defaults());
        }

        @NotNull Builder withLocalCache(@NotNull LocalCacheConfig config);

        default @NotNull Builder withLocalCache(@NotNull LocalCacheConfig.Builder builder) {
            Check.notNull(builder, "builder");
            return this.withLocalCache(builder.build());
        }

        default @NotNull Builder withLocalCache(@NotNull UnaryOperator<LocalCacheConfig.Builder> function) {
            Check.notNull(function, "function");
            return this.withLocalCache(function.apply(LocalCacheConfig.builder()));
        }

        default @NotNull Builder withLocalCache() {
            return this.withLocalCache(LocalCacheConfig.defaults());
        }

        @NotNull Builder serializer(@NotNull NodeSerializer nodeSerializer);

    }
}
