package de.notion.pipeline.example.test;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.annotation.Context;
import de.notion.pipeline.annotation.Properties;
import de.notion.pipeline.annotation.auto.AutoLoad;
import de.notion.pipeline.annotation.auto.AutoSave;
import de.notion.pipeline.datatype.ConnectionPipelineData;
import org.jetbrains.annotations.NotNull;

@Properties(identifier = "TestConnectionData", context = Context.GLOBAL)
@AutoLoad()
@AutoSave(saveToGlobalStorage = true)
public class TestConnectionData extends ConnectionPipelineData {

    public TestConnectionData(@NotNull Pipeline pipeline) {
        super(pipeline);
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onDisconnect() {

    }
}
