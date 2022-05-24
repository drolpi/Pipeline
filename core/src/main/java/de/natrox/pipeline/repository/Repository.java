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

import de.natrox.pipeline.document.find.FindOptions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

@ApiStatus.Experimental
public interface Repository<T> {

    @NotNull Cursor<T> find(@NotNull FindOptions findOptions);

    default @NotNull Cursor<T> find(@NotNull Consumer<FindOptions.@NotNull Builder> consumer) {
        FindOptions.Builder builder = FindOptions.builder();
        consumer.accept(builder);
        return this.find(builder.build());
    }

    default Cursor<T> find() {
        return this.find(FindOptions.DEFAULT);
    }

    boolean exists(@NotNull UUID uniqueId);

    void remove(@NotNull UUID uniqueId);

    void close();

    void drop();

    boolean isDropped();

    boolean isOpen();

    long size();

}
