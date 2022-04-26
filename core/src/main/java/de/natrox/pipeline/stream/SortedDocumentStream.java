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
import de.natrox.pipeline.document.PipeDocument;
import de.natrox.pipeline.sort.DocumentSorter;
import de.natrox.pipeline.sort.SortOrder;
import de.natrox.pipeline.util.Iterables;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public final class SortedDocumentStream implements PipeStream<Pair<UUID, PipeDocument>> {

    private final List<Pair<String, SortOrder>> sortOrder;
    private final PipeStream<Pair<UUID, PipeDocument>> pipeStream;

    public SortedDocumentStream(List<Pair<String, SortOrder>> sortOrder, PipeStream<Pair<UUID, PipeDocument>> pipeStream) {
        this.sortOrder = sortOrder;
        this.pipeStream = pipeStream;
    }

    @Override
    public Iterator<Pair<UUID, PipeDocument>> iterator() {
        if (pipeStream == null)
            return Collections.emptyIterator();

        DocumentSorter documentSorter = new DocumentSorter(sortOrder);

        List<Pair<UUID, PipeDocument>> recordList = Iterables.toList(pipeStream);
        recordList.sort(documentSorter);

        return recordList.iterator();
    }
}
