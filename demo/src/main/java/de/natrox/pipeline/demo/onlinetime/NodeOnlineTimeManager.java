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

package de.natrox.pipeline.demo.onlinetime;

import de.natrox.pipeline.repository.Pipeline;
import de.natrox.pipeline.repository.QueryStrategy;
import de.natrox.pipeline.repository.Repository;
import de.natrox.pipeline.node.DataNode;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ConstantConditions")
public final class NodeOnlineTimeManager implements OnlineTimeManager {

    private final Repository repository;

    public NodeOnlineTimeManager(@NotNull Pipeline pipeline) {
        this.repository = pipeline
            .buildRepository("OnlineTimeData")
            .withGlobalCache(builder -> builder.expireAfterWrite(2, TimeUnit.HOURS).expireAfterAccess(2, TimeUnit.HOURS))
            .withLocalCache(builder -> builder.expireAfterWrite(2, TimeUnit.HOURS).expireAfterAccess(2, TimeUnit.HOURS))
            .build();
    }

    public void handleJoin(@NotNull UUID uuid) {
        DataNode node = this.repository.loadOrCreate(uuid);
        DataNode onlineTimeNode = node.node("onlineTime");
        DataNode lastJoinNode = node.node("lastJoin");

        onlineTimeNode.set(0L);
        lastJoinNode.set(System.currentTimeMillis());

        this.repository.save(uuid, node, QueryStrategy.ALL);
    }

    public void handleQuit(@NotNull UUID uuid) {
        this.repository.load(uuid).ifPresent(node -> {
            DataNode onlineTimeNode = node.node("onlineTime");
            DataNode lastJoinNode = node.node("lastJoin");

            long onlineTime = onlineTimeNode.getAs(long.class);
            long lastJoin = lastJoinNode.getAs(long.class);

            onlineTime += System.currentTimeMillis() - lastJoin;
            onlineTimeNode.set(onlineTime);

            this.repository.save(uuid, node, QueryStrategy.GLOBAL_STORAGE);
            this.repository.remove(uuid, QueryStrategy.LOCAL_CACHE, QueryStrategy.GLOBAL_CACHE);
        });
    }

}
