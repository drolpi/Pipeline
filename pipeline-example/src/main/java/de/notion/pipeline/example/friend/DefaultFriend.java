package de.notion.pipeline.example.friend;

import com.google.inject.Inject;
import de.notion.pipeline.Pipeline;
import de.notion.pipeline.annotation.Context;
import de.notion.pipeline.annotation.PersistentData;
import de.notion.pipeline.annotation.Properties;
import de.notion.pipeline.annotation.auto.AutoCleanUp;
import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Properties(identifier = "Friend", context = Context.GLOBAL)
@AutoCleanUp(saveToGlobalStorage = true, time = 20, timeUnit = TimeUnit.SECONDS)
public class DefaultFriend extends PipelineData {

    @PersistentData
    private UUID sender;

    @PersistentData
    private UUID target;

    @PersistentData
    private boolean approved;

    @Inject
    public DefaultFriend(@NotNull Pipeline pipeline) {
        super(pipeline);
    }

    @Override
    public void onCreate() {
        this.approved = false;
    }

    public UUID sender() {
        return sender;
    }

    public UUID target() {
        return target;
    }

    public boolean approved() {
        return approved;
    }

    public void setSender(UUID sender) {
        this.sender = sender;
    }

    public void setTarget(UUID target) {
        this.target = target;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}
