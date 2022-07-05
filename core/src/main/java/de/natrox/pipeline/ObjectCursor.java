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

package de.natrox.pipeline;

import de.natrox.common.container.Pair;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.object.InstanceCreator;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.stream.PipeStream;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
final class ObjectCursor<T extends ObjectData> implements Cursor<T> {

    private final ObjectRepositoryImpl<T> repository;
    private final PipeStream<Pair<UUID, DocumentData>> pipeStream;
    private final InstanceCreator<T> instanceCreator;

    public ObjectCursor(ObjectRepositoryImpl<T> repository, InstanceCreator<T> instanceCreator, PipeStream<Pair<UUID, DocumentData>> pipeStream) {
        this.repository = repository;
        this.pipeStream = pipeStream;
        this.instanceCreator = instanceCreator;
    }

    @Override
    public long size() {
        return this.pipeStream.size();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new ObjectCursorIterator(this.pipeStream.iterator());
    }

    private class ObjectCursorIterator implements Iterator<T> {

        private final Iterator<Pair<UUID, DocumentData>> documentIterator;

        ObjectCursorIterator(Iterator<Pair<UUID, DocumentData>> documentIterator) {
            this.documentIterator = documentIterator;
        }

        @Override
        public boolean hasNext() {
            return this.documentIterator.hasNext();
        }

        @Override
        public T next() {
            Pair<UUID, DocumentData> next = this.documentIterator.next();
            return repository.convertToData(next.first(), next.second(), instanceCreator);
        }

        @Override
        public void remove() {
            throw new RuntimeException("Remove on a cursor is not supported");
        }
    }
}
