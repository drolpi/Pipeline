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

package de.natrox.pipeline.sort;

import de.natrox.pipeline.PartBundle;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.condition.Conditions;
import de.natrox.pipeline.document.DocumentCursor;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.document.DocumentRepository;
import de.natrox.pipeline.document.find.FindOptions;
import de.natrox.pipeline.jackson.JacksonConverter;
import de.natrox.pipeline.part.memory.InMemoryProvider;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class SortTest {

    @Test
    public void test() {
        PartBundle bundle = PartBundle.local(InMemoryProvider.create());

        Pipeline pipeline = Pipeline
            .builder()
            .bundle(bundle)
            .jsonConverter(JacksonConverter.create())
            .build();

        DocumentRepository repository = pipeline.repository("SortTest");
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < 3000; i++) {
            UUID uniqueId = UUID.randomUUID();
            DocumentData documentData = DocumentData
                .create()
                .append("name", random.nextInt(0, 2) == 0 ? "Aaron" : "Zaher")
                .append("age", random.nextInt(0, 100))
                .append("european", random.nextBoolean());

            repository.insert(uniqueId, documentData);
        }

        DocumentCursor cursor = repository.find(
            FindOptions
                .builder()
                .condition(Conditions.and(Conditions.eq("european", true), Conditions.gt("age", 18)))
                .sort(Sorts.and(Sorts.ascending("name"), Sorts.descending("age")))
                .build()
        );

        for (DocumentData documentData : cursor) {
            System.out.println(documentData);
        }

    }

}
