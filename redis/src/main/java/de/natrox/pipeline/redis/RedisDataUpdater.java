package de.natrox.pipeline.redis;

import com.google.common.base.Preconditions;
import com.google.gson.JsonParser;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

final class RedisDataUpdater extends AbstractDataUpdater {

    private final static Logger LOGGER = LoggerFactory.getLogger(RedisDataUpdater.class);
    private final static String DATA_TOPIC = "DataTopic";

    private final LocalCache localCache;
    private final RedissonClient redissonClient;
    private final RTopic dataTopic;
    private final MessageListener<DataBlock> messageListener;
    private final UUID senderUUID = UUID.randomUUID();

    protected RedisDataUpdater(@NotNull Pipeline pipeline, @NotNull RedissonClient redissonClient) {
        this.localCache = pipeline.localCache();
        this.redissonClient = redissonClient;

        this.dataTopic = topic();
        this.messageListener = (channel, dataBlock) -> {
            if (dataBlock.senderUUID.equals(senderUUID))
                return;
            if (!pipeline.registry().isRegistered(dataBlock.identifier))
                return;
            var dataClass = pipeline.registry().dataClass(dataBlock.identifier);
            var pipelineData = localCache.data(dataClass, dataBlock.dataUUID);

            if (dataBlock instanceof UpdateDataBlock updateDataBlock) {
                var dataToUpdate = JsonParser.parseString(updateDataBlock.dataToUpdate).getAsJsonObject();
                if (pipelineData == null) {
                    this.receivedSync(updateDataBlock.dataUUID, dataToUpdate);
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

        LOGGER.debug("Redis data updater initialized");
    }

    @Override
    public void pushUpdate(@NotNull PipelineData pipelineData, @Nullable Runnable callback) {
        Preconditions.checkNotNull(pipelineData, "pipelineData");
        if (pipelineData.isMarkedForRemoval()) {
            LOGGER.debug("Push rejected as it is marked for removal " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis());
            return;
        }
        pipelineData.unMarkRemoval();
        dataTopic.publish(new UpdateDataBlock(AnnotationResolver.storageIdentifier(pipelineData.getClass()), senderUUID, pipelineData.objectUUID(), pipelineData.serializeToString()));
        LOGGER.debug("Pushing Sync " + pipelineData.objectUUID() + " [" + pipelineData.getClass().getSimpleName() + "] " + System.currentTimeMillis());
        if (callback != null)
            callback.run();
    }

    @Override
    public void pushRemoval(@NotNull PipelineData pipelineData, @Nullable Runnable callback) {
        Preconditions.checkNotNull(pipelineData, "pipelineData");
        pipelineData.markForRemoval();
        dataTopic.publish(new RemoveDataBlock(AnnotationResolver.storageIdentifier(pipelineData.getClass()), senderUUID, pipelineData.objectUUID()));
        LOGGER.debug("Pushing Removal: " + System.currentTimeMillis());
        if (callback != null)
            callback.run();
    }

    @NotNull
    private synchronized RTopic topic() {
        return redissonClient.getTopic(DATA_TOPIC, new SerializationCodec());
    }

    static class RemoveDataBlock extends DataBlock {
        public RemoveDataBlock(@NotNull String identifier, @NotNull UUID senderUUID, @NotNull UUID dataUUID) {
            super(identifier, senderUUID, dataUUID);
        }
    }

    static abstract class DataBlock implements Serializable {

        public final String identifier;
        public final UUID senderUUID;
        public final UUID dataUUID;

        DataBlock(@NotNull String identifier, @NotNull UUID senderUUID, @NotNull UUID dataUUID) {
            this.identifier = identifier;
            this.senderUUID = senderUUID;
            this.dataUUID = dataUUID;
        }
    }

    static class UpdateDataBlock extends DataBlock {

        public final String dataToUpdate;

        public UpdateDataBlock(@NotNull String identifier, @NotNull UUID senderUUID, @NotNull UUID dataUUID, String dataToUpdate) {
            super(identifier, senderUUID, dataUUID);
            this.dataToUpdate = dataToUpdate;
        }
    }
}
