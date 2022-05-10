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
import de.natrox.common.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class BoundedStream<T, U> implements PipeStream<Pair<T, U>> {

    private final PipeStream<Pair<T, U>> pipeStream;
    private final long skip;
    private final long limit;

    public BoundedStream(Long skip, Long limit, PipeStream<Pair<T, U>> pipeStream) {
        Check.argCondition(skip < 0, "skip can not be negative");
        Check.argCondition(limit < 0, "limit can not be negative");

        this.skip = skip;
        this.limit = limit;
        this.pipeStream = pipeStream;
    }

    @Override
    public @NotNull Iterator<Pair<T, U>> iterator() {
        Iterator<Pair<T, U>> iterator = this.pipeStream == null ? Collections.emptyIterator() : this.pipeStream.iterator();
        return new BoundedIterator<>(iterator, this.skip, this.limit);
    }

    private static class BoundedIterator<T> implements Iterator<T> {
        private final Iterator<? extends T> iterator;
        private final long skip;
        private final long limit;
        private long pos;

        public BoundedIterator(final Iterator<? extends T> iterator, final long skip, final long limit) {
            Check.notNull(iterator, "iterator");
            Check.argCondition(skip < 0, "skip can not be negative");
            Check.argCondition(limit < 0, "limit can not be negative");

            this.iterator = iterator;
            this.skip = skip;
            this.limit = limit;
            this.pos = 0;
            this.initialize();
        }

        private void initialize() {
            while (this.pos < this.skip && this.iterator.hasNext()) {
                this.iterator.next();
                this.pos++;
            }
        }

        @Override
        public boolean hasNext() {
            if (this.checkBounds()) {
                return false;
            }
            return this.iterator.hasNext();
        }

        private boolean checkBounds() {
            return this.pos - this.skip + 1 > this.limit;
        }

        @Override
        public T next() {
            if (checkBounds()) {
                throw new NoSuchElementException();
            }
            final T next = this.iterator.next();
            this.pos++;
            return next;
        }

        @Override
        public void remove() {
            if (this.pos <= this.skip) {
                throw new IllegalStateException();
            }
            this.iterator.remove();
        }
    }

}
