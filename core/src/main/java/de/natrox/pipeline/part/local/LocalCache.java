package de.natrox.pipeline.part.local;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public interface LocalCache {

    <S extends PipelineData> @Nullable S get(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID);

    <S extends PipelineData> boolean exists(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID);

    <S extends PipelineData> void save(@NotNull Class<? extends S> dataClass, @NotNull S data);

    <S extends PipelineData> boolean remove(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID);

    <S extends PipelineData> @NotNull Set<UUID> keys(@NotNull Class<? extends S> dataClass);

    <S extends PipelineData> @NotNull Set<S> values(@NotNull Class<? extends S> dataClass);

    <S extends PipelineData> @NotNull Map<UUID, S> entries(@NotNull Class<? extends S> dataClass);

    <S extends PipelineData> @NotNull Map<UUID, S> filter(@NotNull Class<? extends S> dataClass, @NotNull BiPredicate<UUID, S> predicate);

    <S extends PipelineData> void iterate(@NotNull Class<? extends S> dataClass, @NotNull BiConsumer<UUID, S> consumer);

    <S extends PipelineData> @NotNull S instantiateData(Pipeline pipeline, @NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID, @Nullable InstanceCreator<S> instanceCreator);
}
