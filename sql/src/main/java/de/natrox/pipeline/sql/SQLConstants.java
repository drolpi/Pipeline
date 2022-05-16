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

package de.natrox.pipeline.sql;

final class SQLConstants {

    public static final String COLUMN_KEY = "UUID";
    public static final String COLUMN_VAL = "Document";
    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `%s` (%s VARCHAR(64) PRIMARY KEY, %s TEXT);";
    public static final String SELECT_ALL = "SELECT %s FROM `%s`;";
    public static final String SELECT_BY = "SELECT %s FROM `%s` WHERE %s = ?";
    public static final String INSERT_BY = "INSERT INTO `%s` (%s,%s) VALUES (?, ?);";
    public static final String UPDATE_BY = "UPDATE `%s` SET %s=? WHERE %s=?";
    public static final String DELETE_BY = "DELETE FROM `%s` WHERE %s = ?";
    public static final String EVERY = "*";

    private SQLConstants() {
        throw new UnsupportedOperationException();
    }
}
