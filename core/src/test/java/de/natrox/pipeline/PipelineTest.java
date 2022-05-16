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

package de.natrox.pipeline;

import de.natrox.pipeline.part.memory.InMemoryProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PipelineTest {

    @Test
    public void testBuilder() {
        InMemoryProvider provider = InMemoryProvider.create();

        Pipeline.Builder builder = Pipeline.of(provider);
        assertNotNull(builder);
        assertNotNull(builder.build());
    }

    @Test
    public void shutdownTest() {
        Pipeline pipeline = buildPipeline();

        assertFalse(pipeline.isShutDowned(), "The pipeline has not yet been shut down");
        pipeline.shutdown();
        assertTrue(pipeline.isShutDowned(), "The pipeline has already been shut down");
    }

    private Pipeline buildPipeline() {
        return Pipeline
            .of(InMemoryProvider.create())
            .build();
    }

}
