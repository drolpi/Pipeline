package de.notion.pipeline.part;

import com.google.gson.JsonObject;
import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public interface DataProvider {

    JsonObject loadData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID);

    boolean dataExist(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID);

    void saveData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonObject dataToSave);

    boolean removeData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID);

    Set<UUID> savedUUIDs(@NotNull Class<? extends PipelineData> dataClass);

}
