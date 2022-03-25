package de.natrox.pipeline.part.local;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface LocalCache {

    @Nullable <S extends PipelineData> S data(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID);

    @NotNull <S extends PipelineData> Set<S> allData(@NotNull Class<? extends S> dataClass);

    <S extends PipelineData> void save(@NotNull Class<? extends S> dataClass, @NotNull S data);

    <S extends PipelineData> boolean dataExist(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID);

    <S extends PipelineData> boolean remove(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID);

    @NotNull <S extends PipelineData> Set<UUID> savedUUIDs(@NotNull Class<? extends S> dataClass);

    @NotNull <S extends PipelineData> S instantiateData(Pipeline pipeline, @NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID, @Nullable InstanceCreator<S> instanceCreator);
}