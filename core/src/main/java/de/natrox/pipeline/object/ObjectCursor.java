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

package de.natrox.pipeline.object;

import de.natrox.pipeline.document.DocumentCursor;
import de.natrox.pipeline.document.PipeDocument;
import de.natrox.pipeline.repository.Cursor;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

@SuppressWarnings("ClassCanBeRecord")
final class ObjectCursor<T> implements Cursor<T> {

    private final DocumentCursor cursor;

    public ObjectCursor(DocumentCursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public long size() {
        return this.cursor.size();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new ObjectCursorIterator(cursor.iterator());
    }

    private class ObjectCursorIterator implements Iterator<T> {

        private final Iterator<PipeDocument> documentIterator;

        ObjectCursorIterator(Iterator<PipeDocument> documentIterator) {
            this.documentIterator = documentIterator;
        }

        @Override
        public boolean hasNext() {
            return this.documentIterator.hasNext();
        }

        @Override
        public T next() {
            //TODO: Convert document to object
            return null;
        }

        @Override
        public void remove() {
            throw new RuntimeException("remove on a cursor is not supported");
        }
    }
}
