package de.notion.pipeline.part.local;

import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.Pipeline;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface LocalCache {
    @Nullable
    <S extends PipelineData> S getData(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID);

    <S extends PipelineData> Set<S> getAllData(@NotNull Class<? extends S> dataClass);

    <S extends PipelineData> void save(@NotNull Class<? extends S> dataClass, @NotNull S data);

    <S extends PipelineData> boolean dataExist(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID);

    <S extends PipelineData> boolean remove(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID);

    <S extends PipelineData> Set<UUID> getSavedUUIDs(@NotNull Class<? extends S> dataClass);

    <S extends PipelineData> S instantiateData(Pipeline pipeline, @NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID);
}
