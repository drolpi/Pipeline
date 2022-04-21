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

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.Properties;
import de.natrox.pipeline.annotation.property.Context;
import de.natrox.pipeline.datatype.connection.ConnectionData;
import org.jetbrains.annotations.NotNull;

@Properties(identifier = "PlayerData", context = Context.GLOBAL)
public class PlayerData extends ConnectionData {

    public PlayerData(@NotNull Pipeline pipeline) {
        super(pipeline);
    }

    @Override
    public void onConnect() {
        //code here
    }

    @Override
    public void onDisconnect() {
        //code here
    }
}
