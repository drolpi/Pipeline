package de.notion.pipeline.redis.updater;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.local.LocalCache;
import de.notion.pipeline.part.local.updater.DataUpdater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.codec.SerializationCodec;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class RedisDataUpdater implements DataUpdater {

    private final RedissonClient redissonClient;
    private final RTopic dataTopic;
    private final MessageListener<DataBlock> messageListener;
    private final UUID senderUUID = UUID.randomUUID();

    public RedisDataUpdater(@NotNull RedissonClient redissonClient, @NotNull LocalCache localCache, @NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "DataClass can't be null!");
        this.redissonClient = redissonClient;

        this.dataTopic = topic(dataClass);
        this.messageListener = (channel, dataBlock) -> {
            if (dataBlock.senderUUID.equals(senderUUID))
                return;
            PipelineData pipelineData = localCache.data(dataClass, dataBlock.dataUUID);

            if (pipelineData == null)
                return;
            if (dataBlock instanceof UpdateDataBlock) {
                UpdateDataBlock updateDataBlock = (UpdateDataBlock) dataBlock;
                System.out.println("Received Sync " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis()); //DEBUG
                pipelineData.onSync(pipelineData.deserialize(updateDataBlock.dataToUpdate));
            } else if (dataBlock instanceof RemoveDataBlock) {
                System.out.println("Received Removal Instruction " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis()); //DEBUG
                pipelineData.markForRemoval();
                pipelineData.pipeline().delete(pipelineData.getClass(), pipelineData.objectUUID(), false, Pipeline.QueryStrategy.LOCAL);
            }
        };
        dataTopic.addListener(DataBlock.class, messageListener);
    }

    @Override
    public void pushUpdate(@NotNull PipelineData pipelineData, @Nullable Runnable callback) {
        doPush(pipelineData, callback);
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

    private void doPush(@NotNull PipelineData pipelineData, @Nullable Runnable callback) {
        Objects.requireNonNull(pipelineData, "pipelineData can't be null!");
        if (pipelineData.isMarkedForRemoval()) {
            System.out.println("Push rejected as it is marked for removal " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis()); //DEBUG
            return;
        }
        pipelineData.unMarkRemoval();
        dataTopic.publish(new UpdateDataBlock(senderUUID, pipelineData.objectUUID(), pipelineData.serialize()));
        System.out.println("Pushing Sync " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis()); //DEBUG
        if (callback != null)
            callback.run();
    }

    private synchronized RTopic topic(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        String key = "DataTopic:" + AnnotationResolver.storageIdentifier(dataClass);
        return redissonClient.getTopic(key, new SerializationCodec());
    }

    public static class RemoveDataBlock extends DataBlock {
        public RemoveDataBlock(@NotNull UUID senderUUID, @NotNull UUID dataUUID) {
            super(senderUUID, dataUUID);
        }
    }

    public abstract static class DataBlock implements Serializable {
        protected final UUID senderUUID;
        protected final UUID dataUUID;

        DataBlock(@NotNull UUID senderUUID, @NotNull UUID dataUUID) {
            this.senderUUID = senderUUID;
            this.dataUUID = dataUUID;
        }
    }

    public static class UpdateDataBlock extends DataBlock {
        private final String dataToUpdate;

        public UpdateDataBlock(@NotNull UUID senderUUID, @NotNull UUID dataUUID, String dataToUpdate) {
            super(senderUUID, dataUUID);
            this.dataToUpdate = dataToUpdate;
        }
    }
}
