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
import de.natrox.pipeline.document.DocumentCursor;
import de.natrox.pipeline.document.PipeDocument;

import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

public final class DocumentStream implements DocumentCursor {

    private final PipeStream<Pair<UUID, PipeDocument>> pipeStream;

    public DocumentStream(PipeStream<Pair<UUID, PipeDocument>> pipeStream) {
        this.pipeStream = pipeStream;
    }

    @Override
    public Iterator<PipeDocument> iterator() {
        Iterator<Pair<UUID, PipeDocument>> iterator = pipeStream == null ? Collections.emptyIterator() : pipeStream.iterator();
        return new DocumentCursorIterator(iterator);
    }

    private static class DocumentCursorIterator implements Iterator<PipeDocument> {

        private final Iterator<Pair<UUID, PipeDocument>> iterator;

        DocumentCursorIterator(Iterator<Pair<UUID, PipeDocument>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public PipeDocument next() {
            Pair<UUID, PipeDocument> next = iterator.next();
            return next.second();
        }

        @Override
        public void remove() {
            throw new RuntimeException("remove on cursor is not supported");
        }
    }

}
