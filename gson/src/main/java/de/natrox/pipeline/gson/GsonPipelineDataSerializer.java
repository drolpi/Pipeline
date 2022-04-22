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

package de.natrox.pipeline.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import de.natrox.pipeline.old.datatype.PipelineData;
import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.json.serializer.PipelineDataSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

final class GsonPipelineDataSerializer implements PipelineDataSerializer {

    private final Gson gson;
    private final PipelineData data;

    protected GsonPipelineDataSerializer(PipelineData data) {
        this.data = data;
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(data.getClass(), (InstanceCreator<PipelineData>) type -> data)
            .create();
    }

    @Override
    public @UnknownNullability PipelineData toPipelineData(@NotNull JsonDocument document) {
        if (!(document instanceof GsonDocument gsonDocument))
            return null;
        return gson.fromJson(gsonDocument.object, data.getClass());
    }
}
