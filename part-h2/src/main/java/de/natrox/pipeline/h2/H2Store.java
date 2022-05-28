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

package de.natrox.pipeline.h2;

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.pipeline.sql.SqlStore;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

final class H2Store extends SqlStore {

    H2Store(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public @NotNull Set<String> maps() {
        // FIXME: 28.05.2022 Don't list internal table names
        return super.maps();
    }
}
