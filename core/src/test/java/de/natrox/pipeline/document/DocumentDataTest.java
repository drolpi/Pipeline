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

package de.natrox.pipeline.document;

import de.natrox.common.container.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentDataTest {

    private static DocumentData documentData;

    @BeforeAll
    public static void setup() {
        documentData = DocumentData
            .create("name", "Eric")
            .put("level", 234685)
            .put("address.street", "montana-avenue")
            .put("test.name", "Eric");
    }

    @Test
    public void testCreateDocument() {
        assertTrue(DocumentData.create().isEmpty());
        assertEquals(1, DocumentData.create("Key", "Value").size());
        assertTrue(DocumentData.create(new HashMap<>()).isEmpty());
    }

    @Test
    public void testGet() {
        assertNull(documentData.get(""));
        assertEquals(documentData.get("level"), 234685);
        assertEquals(documentData.get("name"), "Eric");
        assertEquals(documentData.get("address.street"), "montana-avenue");
        assertNull(documentData.get("address.number"));

        assertEquals(documentData.get("name"), documentData.get("name"));
        assertEquals(documentData.get("test.name"), documentData.get("name"));

        assertNotEquals(documentData.get("name"), "a");
        assertNull(documentData.get("."));
        assertNull(documentData.get("level.test"));
    }

    @Test
    public void testPutNull() {
        assertNotNull(documentData.put("test", null));
        assertNull(documentData.get("test"));
    }

    @Test
    public void testRemove() {
        Iterator<Pair<String, Object>> iterator = documentData.iterator();
        assertEquals(documentData.size(), 5);
        if (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        assertEquals(documentData.size(), 4);
    }

    @Test
    public void testPut() {
        assertEquals(documentData.size(), 4);
        documentData.put("age", 22);

        assertEquals(documentData.get("age"), 22);
        assertEquals(documentData.size(), 5);
    }

}
