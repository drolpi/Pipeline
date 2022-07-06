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
import de.natrox.pipeline.document.DocumentData;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public final class DocumentStream implements Cursor<DocumentData> {

    private final PipeStream<Pair<UUID, DocumentData>> pipeStream;

    public DocumentStream(PipeStream<Pair<UUID, DocumentData>> pipeStream) {
        this.pipeStream = pipeStream;
    }

    @Override
    public @NotNull Iterator<DocumentData> iterator() {
        return new DocumentCursorIterator(this.pipeStream == null ? Collections.emptyIterator() : this.pipeStream.iterator());
    }

    public @NotNull PipeStream<Pair<UUID, DocumentData>> asPairStream() {
        return this.pipeStream;
    }

    private final static class DocumentCursorIterator implements Iterator<DocumentData> {

        private final Iterator<Pair<UUID, DocumentData>> iterator;

        DocumentCursorIterator(Iterator<Pair<UUID, DocumentData>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public DocumentData next() {
            Pair<UUID, DocumentData> next = this.iterator.next();
            return next.second();
        }

        @Override
        public void remove() {
            throw new RuntimeException("remove operation cannot be called here");
        }
    }

}
