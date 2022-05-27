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
import de.natrox.pipeline.part.updater.Updater;
import de.natrox.pipeline.part.updater.event.DocumentRemoveEvent;
import de.natrox.pipeline.part.updater.event.ByteDocumentUpdateEvent;
import de.natrox.pipeline.part.updater.event.MapClearEvent;
import de.natrox.pipeline.part.updater.event.UpdaterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;

import java.util.UUID;

final class RedisUpdater implements Updater {

    //TODO: Maybe rename???
    private final static String DATA_TOPIC = "DataTopic";

    private final EventBus eventBus;
    private final RTopic dataTopic;
    private final UUID senderId = UUID.randomUUID();

    RedisUpdater(RedissonClient redissonClient) {
        this.eventBus = EventBus.create();
        this.dataTopic = redissonClient.getTopic(DATA_TOPIC, new SerializationCodec());

        this.dataTopic.addListener(UpdaterEvent.class, this::call);
    }

    private void call(CharSequence channel, UpdaterEvent event) {
        if (event == null)
            return;

        if (event.senderId().equals(this.senderId))
            return;

        this.eventBus.call(event);
    }

    @Override
    public void pushUpdate(@NotNull String repositoryName, @NotNull UUID uniqueId, byte @NotNull [] data, @Nullable Runnable callback) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(data, "data");
        this.dataTopic.publish(new ByteDocumentUpdateEvent(this.senderId, repositoryName, uniqueId, data));
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
}
