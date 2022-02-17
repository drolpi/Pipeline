package de.notion.pipeline.example.test;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.annotation.Context;
import de.notion.pipeline.annotation.PersistentData;
import de.notion.pipeline.annotation.Properties;
import de.notion.pipeline.annotation.auto.AutoCleanUp;
import de.notion.pipeline.annotation.auto.AutoLoad;
import de.notion.pipeline.annotation.auto.AutoSave;
import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@Properties(identifier = "TestData", context = Context.GLOBAL)
@AutoLoad()
@AutoSave(saveToGlobalStorage = true)
@AutoCleanUp(saveToGlobalStorage = true, time = 10, timeUnit = TimeUnit.MINUTES)
public class TestData extends PipelineData {

    @PersistentData
    private String name;

    @PersistentData
    private int age;

    public TestData(@NotNull Pipeline pipeline) {
        super(pipeline);
    }

    @Override
    public void onCreate() {
        name = "Peter";
        age = 10;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
