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

package de.natrox.pipeline.object;

import de.natrox.pipeline.PartBundle;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.jackson.JacksonConverter;
import de.natrox.pipeline.object.annotation.Properties;
import de.natrox.pipeline.part.memory.InMemoryProvider;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ObjectTest {

    @Test
    public void test() {
        PartBundle bundle = PartBundle.local(new InMemoryProvider());

        Pipeline pipeline = Pipeline
            .builder()
            .bundle(bundle)
            .jsonConverter(JacksonConverter.create())
            .build();

        ObjectRepository<TestObjectData> repository = pipeline.repository(TestObjectData.class);

        UUID uuid = UUID.nameUUIDFromBytes("Test".getBytes(StandardCharsets.UTF_8));
        TestObjectData data = repository.load(uuid).orElse(null);
    }

    @Properties(identifier = "TestObjectData")
    static class TestObjectData extends ObjectData {



    }

}
