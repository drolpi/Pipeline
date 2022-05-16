/*
 * Copyright 2020-2022 NatroxMC team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.natrox.pipeline.redis;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.part.LocalUpdater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.codec.SerializationCodec;

import java.io.Serializable;
import java.util.UUID;

final class RedisLocalUpdater implements LocalUpdater {

    //TODO: Maybe rename???
    private final static String DATA_TOPIC = "DataTopic";

    private final JsonConverter jsonConverter;
    private final RTopic dataTopic;
    private final UUID senderId = UUID.randomUUID();

    RedisLocalUpdater(Pipeline pipeline, RedissonClient redissonClient) {
        this.jsonConverter = pipeline.jsonConverter();
        this.dataTopic = redissonClient.getTopic(DATA_TOPIC, new SerializationCodec());

        //TODO:
        MessageListener<DataBlock> messageListener = (channel, dataBlock) -> {
            //TODO:
        };
        this.dataTopic.addListener(DataBlock.class, messageListener);
    }

    @Override
    public void pushUpdate(@NotNull UUID uniqueId, @NotNull DocumentData documentData, @Nullable Runnable callback) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(documentData, "documentData");
        this.dataTopic.publish(new UpdateDataBlock(this.senderId, uniqueId, this.jsonConverter.writeAsString(documentData)));
        if (callback != null)
            callback.run();
    }

    @Override
    public void pushRemoval(@NotNull UUID uniqueId, @Nullable Runnable callback) {
        Check.notNull(uniqueId, "uniqueId");
        this.dataTopic.publish(new RemoveDataBlock(this.senderId, uniqueId));
        if (callback != null)
            callback.run();
    }

    @Override
    public void pushClear(@Nullable Runnable callback) {
        this.dataTopic.publish(new ClearDataBlock(this.senderId));
        if (callback != null)
            callback.run();
    }

    static abstract class DataBlock implements Serializable {

        public final UUID senderId;

        DataBlock(@NotNull UUID senderId) {
            this.senderId = senderId;
        }
    }

    static abstract class DocumentDataBlock extends DataBlock {

        public final UUID documentId;

        DocumentDataBlock(@NotNull UUID senderId, @NotNull UUID documentId) {
            super(senderId);
            this.documentId = documentId;
        }
    }

    static class ClearDataBlock extends DataBlock {

        public ClearDataBlock(@NotNull UUID senderId) {
            super(senderId);
        }
    }

    static class UpdateDataBlock extends DocumentDataBlock {

        public final String dataToUpdate;

        public UpdateDataBlock(@NotNull UUID senderId, @NotNull UUID documentId, String dataToUpdate) {
            super(senderId, documentId);
            this.dataToUpdate = dataToUpdate;
        }
    }

    static class RemoveDataBlock extends DocumentDataBlock {

        public RemoveDataBlock(@NotNull UUID senderId, @NotNull UUID documentId) {
            super(senderId, documentId);
        }
    }
}
