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
import de.natrox.pipeline.sort.DocumentSorter;
import de.natrox.pipeline.sort.SortOrder;
import de.natrox.pipeline.node.DataNode;
import de.natrox.pipeline.util.Iterables;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public final class SortedStream implements DataStream<Pair<UUID, DataNode>> {

    private final List<Pair<Object[], SortOrder>> sortOrder;
    private final DataStream<Pair<UUID, DataNode>> dataStream;

    public SortedStream(List<Pair<Object[], SortOrder>> sortOrder, DataStream<Pair<UUID, DataNode>> dataStream) {
        this.sortOrder = sortOrder;
        this.dataStream = dataStream;
    }

    @Override
    public @NotNull Iterator<Pair<UUID, DataNode>> iterator() {
        if (this.dataStream == null)
            return Collections.emptyIterator();

        DocumentSorter documentSorter = new DocumentSorter(this.sortOrder);

        List<Pair<UUID, DataNode>> recordList = Iterables.toList(this.dataStream);
        recordList.sort(documentSorter);

        return recordList.iterator();
    }
}
