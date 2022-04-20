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

package de.natrox.pipeline.test;

import de.natrox.pipeline.mysql.MySqlConfig;
import de.natrox.pipeline.mysql.MySqlEndpoint;
import de.natrox.pipeline.mysql.MySqlProvider;

public class MySqlTest {

    public static void main(String[] args) throws Exception{
        MySqlConfig mySqlConfig = MySqlConfig
            .builder()
            .endpoints(
                MySqlEndpoint
                    .builder()
                    .host("0.0.0.0")
                    .port(3306)
                    .database("database")
                    .useSsl(true)
                    .build(),
                MySqlEndpoint
                    .builder()
                    .host("0.0.0.1")
                    .port(3306)
                    .database("database")
                    .useSsl(true)
                    .build()
            )
            .username("username")
            .password("password")
            .build();
        MySqlProvider mySqlProvider = mySqlConfig.createProvider();
    }

}
