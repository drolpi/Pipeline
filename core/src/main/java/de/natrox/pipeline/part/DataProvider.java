package de.natrox.pipeline.part;

import com.google.gson.JsonObject;
import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface DataProvider {

    @Nullable
    JsonObject loadData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID);

    boolean dataExist(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID);

    void saveData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonObject dataToSave);

    boolean removeData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID);

    @NotNull
    Collection<UUID> savedUUIDs(@NotNull Class<? extends PipelineData> dataClass);

}
