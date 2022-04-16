package de.natrox.pipeline.test;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.Properties;
import de.natrox.pipeline.annotation.property.Context;
import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RegisterTest {

    public static void main(String[] args) throws Exception {
        var pipeline = Pipeline
            .builder()
            .registry(new PipelineRegistry())
            .build();

        pipeline.load(TestData.class, UUID.randomUUID(), Pipeline.LoadingStrategy.LOAD_PIPELINE);
    }

    @Properties(identifier = "Test", context = Context.GLOBAL)
    static class TestData extends PipelineData {

        public TestData(@NotNull Pipeline pipeline) {
            super(pipeline);
        }
    }

}
