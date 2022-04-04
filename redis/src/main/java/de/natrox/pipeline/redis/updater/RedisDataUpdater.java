package de.natrox.pipeline.redis.updater;

import com.google.gson.JsonParser;
import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.part.local.LocalCache;
import de.natrox.pipeline.part.updater.AbstractDataUpdater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.codec.SerializationCodec;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class RedisDataUpdater extends AbstractDataUpdater {

    private final static Logger LOGGER = LogManager.logger(RedisDataUpdater.class);
    private final static String DATA_TOPIC = "DataTopic:%s";

    private final LocalCache localCache;
    private final RedissonClient redissonClient;
    private final RTopic dataTopic;
    private final MessageListener<DataBlock> messageListener;
    private final UUID senderUUID = UUID.randomUUID();

    public RedisDataUpdater(@NotNull Pipeline pipeline, @NotNull RedissonClient redissonClient, @NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "DataClass can't be null!");
        this.localCache = pipeline.localCache();
        this.redissonClient = redissonClient;

        this.dataTopic = topic(dataClass);
        this.messageListener = (channel, dataBlock) -> {
            if (dataBlock.senderUUID.equals(senderUUID))
                return;
            var pipelineData = localCache.data(dataClass, dataBlock.dataUUID);

            if (dataBlock instanceof UpdateDataBlock updateDataBlock) {
                var dataToUpdate = JsonParser.parseString(updateDataBlock.dataToUpdate).getAsJsonObject();
                if (pipelineData == null) {
                    receivedData(updateDataBlock.dataUUID, dataToUpdate);
                } else {
                    pipelineData.onSync(pipelineData.deserialize(dataToUpdate));
                    LOGGER.debug("Received Sync " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis()); 
                }
            } else if (dataBlock instanceof RemoveDataBlock) {
                if (pipelineData == null)
                    return;
                LOGGER.debug("Received Removal Instruction " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis()); 
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
            LOGGER.debug("Push rejected as it is marked for removal " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis()); 
            return;
        }
        pipelineData.unMarkRemoval();
        dataTopic.publish(new UpdateDataBlock(senderUUID, pipelineData.objectUUID(), pipelineData.serializeToString()));
        LOGGER.debug("Pushing Sync " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis()); 
        if (callback != null)
            callback.run();
    }

    @Override
    public void pushRemoval(@NotNull PipelineData pipelineData, @Nullable Runnable callback) {
        Objects.requireNonNull(pipelineData, "pipelineData can't be null!");
        pipelineData.markForRemoval();
        dataTopic.publish(new RemoveDataBlock(senderUUID, pipelineData.objectUUID()));
        LOGGER.debug("Pushing Removal: " + System.currentTimeMillis()); 
        if (callback != null)
            callback.run();
    }

    @NotNull
    private synchronized RTopic topic(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        var key = String.format(DATA_TOPIC, AnnotationResolver.storageIdentifier(dataClass));
        return redissonClient.getTopic(key, new SerializationCodec());
    }

    static class RemoveDataBlock extends DataBlock {
        public RemoveDataBlock(@NotNull UUID senderUUID, @NotNull UUID dataUUID) {
            super(senderUUID, dataUUID);
        }
    }

    static abstract class DataBlock implements Serializable {

        public final UUID senderUUID;
        public final UUID dataUUID;

        DataBlock(@NotNull UUID senderUUID, @NotNull UUID dataUUID) {
            this.senderUUID = senderUUID;
            this.dataUUID = dataUUID;
        }
    }

    static class UpdateDataBlock extends DataBlock {

        public final String dataToUpdate;

        public UpdateDataBlock(@NotNull UUID senderUUID, @NotNull UUID dataUUID, String dataToUpdate) {
            super(senderUUID, dataUUID);
            this.dataToUpdate = dataToUpdate;
        }
    }
}
