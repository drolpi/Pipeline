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

import de.natrox.pipeline.h2.H2Config;
import de.natrox.pipeline.h2.H2Provider;

import java.nio.file.Path;

public class H2Test {

    public static void main(String[] args) throws Exception {
        H2Config h2Config = H2Config
            .builder()
            .path(Path.of("storage", "database"))
            .build();
        H2Provider h2Provider = h2Config.createProvider();
    }

}
