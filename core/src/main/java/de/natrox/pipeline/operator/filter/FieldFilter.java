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

package de.natrox.pipeline.operator.filter;

import de.natrox.pipeline.json.document.JsonDocument;
import org.jetbrains.annotations.NotNull;

public record FieldFilter(@NotNull String fieldName, @NotNull Object obj) implements Filter {

    @Override
    public boolean check(@NotNull JsonDocument data) {
        for (var key : data.keys()) {
            if (key == null)
                continue;
            var value = data.get(key);
            if (value == null)
                continue;

            if (key.equals(fieldName) && value.equals(obj)) {
                return true;
            }
        }
        return false;
    }
}
