/*
 * Copyright 2020-2022 NatroxMC
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
import de.natrox.eventbus.EventBus;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.part.updater.event.DocumentEvent;
import de.natrox.pipeline.part.updater.event.DocumentRemoveEvent;
import de.natrox.pipeline.part.updater.event.DocumentUpdateEvent;
import de.natrox.pipeline.part.updater.event.MapClearEvent;
import de.natrox.pipeline.part.updater.Updater;
import de.natrox.pipeline.part.updater.event.UpdaterEvent;
import de.natrox.pipeline.mapper.DocumentMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;

import java.util.UUID;

final class RedisUpdater implements Updater {

    //TODO: Maybe rename???
    private final static String DATA_TOPIC = "DataTopic";

    private final DocumentMapper documentMapper;
    private final EventBus eventBus;
    private final RTopic dataTopic;
    private final UUID senderId = UUID.randomUUID();

    RedisUpdater(Pipeline pipeline, RedissonClient redissonClient) {
        this.documentMapper = pipeline.documentMapper();
        this.eventBus = EventBus.create();
        this.dataTopic = redissonClient.getTopic(DATA_TOPIC, new SerializationCodec());

        this.dataTopic.addListener(UpdaterEvent.class, this::call);
    }

    private void call(CharSequence channel, UpdaterEvent event) {
        if (event == null)
            return;

        if (event.senderId().equals(this.senderId))
            return;

        if (event instanceof RedisDocumentUpdateEvent redisEvent)
            event = new DocumentUpdateEvent(
                redisEvent.senderId(),
                redisEvent.repositoryName(),
                redisEvent.documentId(),
                this.mapper.read(redisEvent.documentData(), DocumentData.class)
            );

        this.eventBus.call(event);
    }

    @Override
    public void pushUpdate(@NotNull String repositoryName, @NotNull UUID uniqueId, @NotNull DocumentData documentData, @Nullable Runnable callback) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(documentData, "documentData");
        this.dataTopic.publish(new RedisDocumentUpdateEvent(this.senderId, repositoryName, uniqueId, this.documentMapper.writeAsString(documentData)));
        if (callback != null)
            callback.run();
    }

    @Override
    public void pushRemoval(@NotNull String repositoryName, @NotNull UUID uniqueId, @Nullable Runnable callback) {
        Check.notNull(uniqueId, "uniqueId");
        this.dataTopic.publish(new DocumentRemoveEvent(this.senderId, repositoryName, uniqueId));
        if (callback != null)
            callback.run();
    }

    @Override
    public void pushClear(@NotNull String repositoryName, @Nullable Runnable callback) {
        this.dataTopic.publish(new MapClearEvent(this.senderId, repositoryName));
        if (callback != null)
            callback.run();
    }

    @Override
    public @NotNull EventBus eventBus() {
        return this.eventBus;
    }

    static final class RedisDocumentUpdateEvent extends DocumentEvent {

        private final String documentData;

        public RedisDocumentUpdateEvent(@NotNull UUID senderId, @NotNull String repositoryName, @NotNull UUID documentId, String documentData) {
            super(senderId, repositoryName, documentId);
            this.documentData = documentData;
        }

        public String documentData() {
            return this.documentData;
        }
    }
}
