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

package de.natrox.pipeline.stream;

import de.natrox.common.container.Pair;
import de.natrox.pipeline.condition.Condition;
import de.natrox.pipeline.node.DataNode;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public final class ConditionalStream implements DataStream<Pair<UUID, DataNode>> {

    private final Condition condition;
    private final DataStream<Pair<UUID, DataNode>> dataStream;

    public ConditionalStream(Condition condition, DataStream<Pair<UUID, DataNode>> dataStream) {
        this.condition = condition;
        this.dataStream = dataStream;
    }

    @Override
    public @NotNull Iterator<Pair<UUID, DataNode>> iterator() {
        Iterator<Pair<UUID, DataNode>> iterator = this.dataStream == null ? Collections.emptyIterator() : this.dataStream.iterator();

        if (this.condition == null)
            return iterator;
        return new ConditionIterator(iterator, this.condition);
    }

    private final static class ConditionIterator implements Iterator<Pair<UUID, DataNode>> {
        private final Iterator<Pair<UUID, DataNode>> iterator;
        private final Condition condition;
        private Pair<UUID, DataNode> nextPair;
        private boolean nextPairSet = false;

        public ConditionIterator(Iterator<Pair<UUID, DataNode>> iterator, Condition condition) {
            this.iterator = iterator;
            this.condition = condition;
        }

        @Override
        public boolean hasNext() {
            return this.nextPairSet || this.setNextId();
        }

        @Override
        public Pair<UUID, DataNode> next() {
            if (!this.nextPairSet && !this.setNextId()) {
                throw new NoSuchElementException();
            }
            this.nextPairSet = false;
            return this.nextPair;
        }

        @Override
        public void remove() {
            if (this.nextPairSet) {
                throw new RuntimeException("remove operation cannot be called here");
            }
            this.iterator.remove();
        }

        private boolean setNextId() {
            while (this.iterator.hasNext()) {
                final Pair<UUID, DataNode> pair = this.iterator.next();
                if (this.condition.apply(pair)) {
                    this.nextPair = pair;
                    this.nextPairSet = true;
                    return true;
                }
            }
            return false;
        }
    }
}
