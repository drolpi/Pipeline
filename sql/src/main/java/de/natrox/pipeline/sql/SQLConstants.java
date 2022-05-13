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

public final class SQLConstants {

    public static final String TABLE_COLUMN_KEY = "UUID";
    public static final String TABLE_COLUMN_VAL = "Document";
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `%s` (%s VARCHAR(64) PRIMARY KEY, %s TEXT);";
    public static final String SELECT_ALL = "SELECT %s FROM `%s`;";
    public static final String SELECT_BY_UUID = "SELECT %s FROM `%s` WHERE %s = ?";
    public static final String INSERT_BY_UUID = "INSERT INTO `%s` (%s,%s) VALUES (?, ?);";
    public static final String UPDATE_BY_UUID = "UPDATE `%s` SET %s=? WHERE %s=?";
    public static final String DELETE_BY_UUID = "DELETE FROM `%s` WHERE %s = ?";

    private SQLConstants() {
        throw new UnsupportedOperationException();
    }
}
