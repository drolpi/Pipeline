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

import de.natrox.pipeline.PartBundle;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.document.DocumentRepository;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.mongo.MongoConfig;
import de.natrox.pipeline.mongo.MongoProvider;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class JsonTest {

    @Test
    public void test() {
        MongoConfig mongoConfig = MongoConfig
            .builder()
            .host("127.0.0.1")
            .port(27017)
            .database("rewrite")
            .build();
        MongoProvider mongoProvider = mongoConfig.createProvider();

        PartBundle bundle = PartBundle.global(mongoProvider);
        JsonConverter jsonConverter = GsonConverter.create();

        Pipeline pipeline = Pipeline
            .builder()
            .bundle(bundle)
            .jsonConverter(jsonConverter)
            .build();

        DocumentRepository repository = pipeline.repository("JsonTest");
        UUID uniqueId = UUID.nameUUIDFromBytes("Test".getBytes(StandardCharsets.UTF_8));
        DocumentData documentData = DocumentData
            .create()
            .append("name", "Peter")
            .append("age", 23);

        repository.insert(uniqueId, documentData);

        repository.get(uniqueId).ifPresent(System.out::println);

    }

}
