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

package de.natrox.pipeline.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.document.DocumentDataImpl;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

@ApiStatus.Internal
final class DocumentDataDeserializer extends StdDeserializer<DocumentData> {

    DocumentDataDeserializer() {
        super(DocumentData.class);
    }

    @Override
    public DocumentData deserialize(JsonParser p, DeserializationContext context) throws IOException {
        return context.readValue(p, DocumentDataImpl.class);
    }
}
