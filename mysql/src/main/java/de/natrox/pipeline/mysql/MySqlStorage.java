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

package de.natrox.pipeline.mysql;

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.sql.SqlStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MySqlStorage extends SqlStorage {

    protected final static Logger LOGGER = LoggerFactory.getLogger(MySqlStorage.class);

    protected MySqlStorage(Pipeline pipeline, HikariDataSource hikariDataSource) {
        super(pipeline, hikariDataSource);

        LOGGER.debug("MySql storage initialized");
    }
}
