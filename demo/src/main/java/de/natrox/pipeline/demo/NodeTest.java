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

package de.natrox.pipeline.demo;

import de.natrox.pipeline.condition.Conditions;
import de.natrox.pipeline.node.DataNode;
import de.natrox.pipeline.part.memory.InMemoryProvider;
import de.natrox.pipeline.repository.Pipeline;
import de.natrox.pipeline.repository.QueryStrategy;
import de.natrox.pipeline.repository.Repository;
import de.natrox.pipeline.serializer.kryo.KryoNodeSerializer;
import de.natrox.pipeline.sort.Sorts;

import java.util.UUID;

class NodeTest {

    void test() {
        InMemoryProvider inMemoryProvider = InMemoryProvider.create();
        KryoNodeSerializer kryoNodeSerializer = KryoNodeSerializer.create();

        Pipeline pipeline = Pipeline
            .builder(inMemoryProvider)
            .build();

        Repository repository = pipeline
            .buildRepository("Account")
            .serializer(kryoNodeSerializer)
            .build();

        UUID uniqueId = UUID.randomUUID();
        DataNode accountNode = repository.loadOrCreate(uniqueId);

        DataNode firstNameNode = accountNode.node("firstName");
        firstNameNode.set("Max");

        DataNode lastNameNode = accountNode.node("lastName");
        lastNameNode.set("Musterman");

        DataNode ageNode = accountNode.node("age");
        ageNode.set(25);

        repository.save(uniqueId, accountNode, QueryStrategy.ALL);

        DataNode foundNode = repository.find(builder -> builder.condition(Conditions.where("age").eq(15)).sort(Sorts.ascending("name"))).first().orElseThrow();

        String firstName = foundNode.node("firstName").getAs(String.class);
        String lastName = foundNode.node("lastName").getAs(String.class);
    }
}
