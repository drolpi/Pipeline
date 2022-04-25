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

package de.natrox.pipeline.stream;

import de.natrox.common.container.Pair;
import de.natrox.pipeline.condition.Condition;
import de.natrox.pipeline.condition.Conditions;
import de.natrox.pipeline.document.PipeDocument;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;

public final class ConditionStream implements PipeStream<Pair<UUID, PipeDocument>> {

    private final PipeStream<Pair<UUID, PipeDocument>> pipeStream;
    private final Condition condition;

    public ConditionStream(PipeStream<Pair<UUID, PipeDocument>> pipeStream, Condition condition) {
        this.pipeStream = pipeStream;
        this.condition = condition;
    }

    @Override
    public Iterator<Pair<UUID, PipeDocument>> iterator() {
        Iterator<Pair<UUID, PipeDocument>> iterator = pipeStream == null ? Collections.emptyIterator() : pipeStream.iterator();

        if (condition == null || condition == Conditions.ALL) {
            return iterator;
        }
        return new ConditionIterator(iterator, condition);
    }

    private static class ConditionIterator implements Iterator<Pair<UUID, PipeDocument>> {
        private final Iterator<Pair<UUID, PipeDocument>> iterator;
        private final Condition condition;
        private Pair<UUID, PipeDocument> nextPair;
        private boolean nextPairSet = false;

        public ConditionIterator(Iterator<Pair<UUID, PipeDocument>> iterator, Condition condition) {
            this.iterator = iterator;
            this.condition = condition;
        }

        @Override
        public boolean hasNext() {
            return nextPairSet || setNextId();
        }

        @Override
        public Pair<UUID, PipeDocument> next() {
            if (!nextPairSet && !setNextId()) {
                throw new NoSuchElementException();
            }
            nextPairSet = false;
            return nextPair;
        }

        @Override
        public void remove() {
            if (nextPairSet) {
                throw new RuntimeException("remove operation cannot be called here");
            }
            iterator.remove();
        }

        private boolean setNextId() {
            while (iterator.hasNext()) {
                final Pair<UUID, PipeDocument> pair = iterator.next();
                if (condition.apply(pair)) {
                    nextPair = pair;
                    nextPairSet = true;
                    return true;
                }
            }
            return false;
        }
    }
}
