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

package de.natrox.pipeline.operator;

import de.natrox.pipeline.operator.filter.Filter;
import org.jetbrains.annotations.Nullable;

public final class FindOptions {

    private Filter filter;
    @Deprecated
    private Object sort;
    private int limit;
    private int skip;

    public FindOptions() {
        this.limit = -1;
        this.skip = -1;
    }

    public @Nullable Filter filter() {
        return this.filter;
    }

    public void setFilter(@Nullable Filter filter) {
        this.filter = filter;
    }

    @Deprecated
    public @Nullable Object sort() {
        return this.sort;
    }

    @Deprecated
    public void setSort(@Nullable Object sort) {
        this.sort = sort;
    }

    public int limit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int skip() {
        return this.skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }
}
