package de.notion.pipeline.redis.updater;

import com.google.gson.JsonParser;
import de.notion.pipeline.Pipeline;
import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.local.LocalCache;
import de.notion.pipeline.part.local.updater.DataUpdater;
import de.notion.pipeline.part.local.updater.LoadingTaskSynchronizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.codec.SerializationCodec;

import java.util.Objects;
import java.util.UUID;

public class RedisDataUpdater implements DataUpdater {

    private final static String DATA_TOPIC = "DataTopic:%s";

    private final LocalCache localCache;
    private final RedissonClient redissonClient;
    private final RTopic dataTopic;
    private final MessageListener<DataBlock> messageListener;
    private final UUID senderUUID = UUID.randomUUID();
    private final LoadingTaskSynchronizer loadingTaskSynchronizer;

    public RedisDataUpdater(@NotNull Pipeline pipeline, @NotNull RedissonClient redissonClient, @NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "DataClass can't be null!");
        this.localCache = pipeline.localCache();
        this.redissonClient = redissonClient;

        this.dataTopic = topic(dataClass);
        this.loadingTaskSynchronizer = new LoadingTaskSynchronizer();
        this.messageListener = (channel, dataBlock) -> {
            if (dataBlock.senderUUID.equals(senderUUID))
                return;
            var pipelineData = localCache.data(dataClass, dataBlock.dataUUID);

            if (dataBlock instanceof UpdateDataBlock updateDataBlock) {
                var dataToUpdate = JsonParser.parseString(updateDataBlock.dataToUpdate).getAsJsonObject();
                if (pipelineData == null) {
                    loadingTaskSynchronizer.updateData(updateDataBlock.dataUUID, dataToUpdate);
                } else {
                    pipelineData.onSync(pipelineData.deserialize(dataToUpdate));
                    System.out.println("Received Sync " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis()); //DEBUG
                }
            } else if (dataBlock instanceof RemoveDataBlock) {
                if (pipelineData == null)
                    return;
                System.out.println("Received Removal Instruction " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis()); //DEBUG
                pipelineData.markForRemoval();
                pipeline.delete(pipelineData.getClass(), pipelineData.objectUUID(), false, Pipeline.QueryStrategy.LOCAL);
            }
        };
        dataTopic.addListener(DataBlock.class, messageListener);
    }

    @Override
    public void pushUpdate(@NotNull PipelineData pipelineData, @Nullable Runnable callback) {
        Objects.requireNonNull(pipelineData, "pipelineData can't be null!");
        if (pipelineData.isMarkedForRemoval()) {
            System.out.println("Push rejected as it is marked for removal " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis()); //DEBUG
            return;
        }
        pipelineData.unMarkRemoval();
        dataTopic.publish(new UpdateDataBlock(senderUUID, pipelineData.objectUUID(), pipelineData.serializeToString()));
        System.out.println("Pushing Sync " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis()); //DEBUG
        if (callback != null)
            callback.run();
    }

    @Override
    public void pushRemoval(@NotNull PipelineData pipelineData, @Nullable Runnable callback) {
        Objects.requireNonNull(pipelineData, "pipelineData can't be null!");
        pipelineData.markForRemoval();
        dataTopic.publish(new RemoveDataBlock(senderUUID, pipelineData.objectUUID()));
        System.out.println("Pushing Removal: " + System.currentTimeMillis());
        if (callback != null)
            callback.run();
    }

    @Override
    public @NotNull LoadingTaskSynchronizer loadingTaskManager() {
        return loadingTaskSynchronizer;
    }

    @NotNull
    private synchronized RTopic topic(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        var key = String.format(DATA_TOPIC, AnnotationResolver.storageIdentifier(dataClass));
        return redissonClient.getTopic(key, new SerializationCodec());
    }
}
