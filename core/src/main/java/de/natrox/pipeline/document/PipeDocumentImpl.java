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

import com.google.common.base.Strings;
import de.natrox.common.container.Pair;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.util.Iterables;
import de.natrox.pipeline.util.ObjectUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PipeDocumentImpl extends LinkedHashMap<String, Object> implements PipeDocument {

    private final static String FIELD_SEPARATOR = ".";
    private final static String DOC_ID = "_id";

    PipeDocumentImpl() {
        super();
    }

    PipeDocumentImpl(Map<String, Object> objectMap) {
        super(objectMap);
    }

    @Override
    public @NotNull PipeDocument put(@NotNull String field, @NotNull Object value) {
        Check.argCondition(Strings.isNullOrEmpty(field), "field is empty or null key");

        if (isEmbedded(field)) {
            String regex = MessageFormat.format("\\{0}", FIELD_SEPARATOR);
            String[] splits = field.split(regex);
            deepPut(splits, value);
        } else {
            super.put(field, value);
        }
        return this;
    }

    @Override
    public @Nullable Object get(@NotNull String field) {
        Check.notNull(field, "field");
        if (isEmbedded(field) && !containsKey(field))
            return deepGet(field);
        return super.get(field);
    }

    @Override
    public <T> @Nullable T get(@NotNull String field, @NotNull Class<T> type) {
        Check.notNull(type, "type");
        return type.cast(get(field));
    }

    @Override
    public @NotNull UUID uniqueId() {
        try {
            if (!containsKey(DOC_ID)) {
                super.put(DOC_ID, UUID.randomUUID());
            }
            return get(DOC_ID, UUID.class);
        } catch (ClassCastException cce) {
            throw new RuntimeException("invalid _id found " + get(DOC_ID));
        }
    }

    @Override
    public @NotNull Set<String> getFields() {
        return getFieldsInternal("");
    }

    @Override
    public void remove(@NotNull String field) {
        Check.notNull(field, "field");
        if (isEmbedded(field)) {
            String regex = MessageFormat.format("\\{0}", FIELD_SEPARATOR);
            String[] splits = field.split(regex);
            deepRemove(splits);
        } else {
            super.remove(field);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull PipeDocument clone() {
        Map<String, Object> cloned = (Map<String, Object>) super.clone();

        for (Map.Entry<String, Object> entry : cloned.entrySet()) {
            if (entry.getValue() instanceof PipeDocument value) {
                PipeDocument clonedValue = value.clone();
                cloned.put(entry.getKey(), clonedValue);
            }
        }
        return new PipeDocumentImpl(cloned);
    }

    @Override
    public @NotNull PipeDocument merge(@NotNull PipeDocument document) {
        Check.notNull(document, "document");
        if (document instanceof PipeDocumentImpl doc) {
            super.putAll(doc);
        }
        return this;
    }

    @Override
    public boolean containsKey(@NotNull String key) {
        Check.notNull(key, "key");
        return super.containsKey(key);
    }

    @Override
    public boolean equals(Object other) {
        Check.notNull(other, "other");
        if (other == this)
            return true;

        if (!(other instanceof PipeDocumentImpl m))
            return false;

        if (m.size() != size())
            return false;

        try {
            for (Map.Entry<String, Object> e : entrySet()) {
                String key = e.getKey();
                Object value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key)))
                        return false;
                } else {
                    if (!Objects.deepEquals(value, m.get(key))) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }

        return true;
    }

    @Override
    public @NotNull Iterator<Pair<String, Object>> iterator() {
        return new MapPairIterator(super.entrySet().iterator());
    }

    private Set<String> getFieldsInternal(String prefix) {
        Set<String> fields = new HashSet<>();

        for (Pair<String, Object> entry : this) {

            Object value = entry.second();
            if (value instanceof PipeDocumentImpl document) {
                if (Strings.isNullOrEmpty(prefix)) {
                    fields.addAll(document.getFieldsInternal(entry.first()));
                } else {
                    fields.addAll(document.getFieldsInternal(prefix
                        + FIELD_SEPARATOR + entry.first()));
                }
            } else if (!(value instanceof Iterable)) {
                if (Strings.isNullOrEmpty(prefix)) {
                    fields.add(entry.first());
                } else {
                    fields.add(prefix + FIELD_SEPARATOR + entry.first());
                }
            }
        }
        return fields;
    }

    private Object deepGet(String field) {
        if (isEmbedded(field)) {
            return getByEmbeddedKey(field);
        } else {
            return null;
        }
    }

    private void deepPut(String[] splits, Object value) {
        if (splits.length == 0) {
            throw new RuntimeException("invalid key provided");
        }
        String key = splits[0];
        if (splits.length == 1) {
            put(key, value);
        } else {
            Object val = get(key);

            String[] remaining = Arrays.copyOfRange(splits, 1, splits.length);

            if (val instanceof PipeDocumentImpl document) {
                document.deepPut(remaining, value);
            } else if (val == null) {
                PipeDocumentImpl subDoc = new PipeDocumentImpl();
                subDoc.deepPut(remaining, value);

                put(key, subDoc);
            }
        }
    }

    private void deepRemove(String[] splits) {
        if (splits.length == 0) {
            throw new RuntimeException("invalid key provided");
        }
        String key = splits[0];
        if (splits.length == 1) {
            remove(key);
        } else {
            Object val = get(key);

            String[] remaining = Arrays.copyOfRange(splits, 1, splits.length);

            if (val instanceof PipeDocumentImpl subDoc) {
                subDoc.deepRemove(remaining);
                if (subDoc.size() == 0) {
                    super.remove(key);
                }
            } else if (val == null) {
                super.remove(key);
            }
        }
    }

    private Object getByEmbeddedKey(String embeddedKey) {
        String regex = MessageFormat.format("\\{0}", FIELD_SEPARATOR);

        String[] path = embeddedKey.split(regex);
        if (path.length < 1) {
            return null;
        }

        return recursiveGet(get(path[0]), Arrays.copyOfRange(path, 1, path.length));
    }

    @SuppressWarnings("unchecked")
    private Object recursiveGet(Object object, String[] remainingPath) {
        if (object == null) {
            return null;
        }

        if (remainingPath.length == 0) {
            return object;
        }

        if (object instanceof PipeDocument) {
            return recursiveGet(((PipeDocument) object).get(remainingPath[0]),
                Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
        }

        if (object.getClass().isArray()) {
            String accessor = remainingPath[0];
            Object[] array = ObjectUtil.convertToObjectArray(object);

            if (isInteger(accessor)) {
                int index = asInteger(accessor);
                if (index < 0) {
                    throw new RuntimeException("invalid array index " + index + " to access item inside a document");
                }

                if (index >= array.length)
                    throw new RuntimeException("index " + index +
                        " is not less than the size of the array " + array.length);
                return recursiveGet(array[index], Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
            } else {
                return decompose(Arrays.asList(array), remainingPath);
            }
        }

        if (object instanceof Iterable) {
            String accessor = remainingPath[0];
            Iterable<Object> iterable = (Iterable<Object>) object;
            List<Object> collection = Iterables.toList(iterable);

            if (isInteger(accessor)) {
                int index = asInteger(accessor);
                if (index < 0)
                    throw new RuntimeException("invalid collection index " + index + " to access item inside a document");

                if (index >= collection.size())
                    throw new RuntimeException("index " + accessor +
                        " is not less than the size of the list " + collection.size());

                return recursiveGet(collection.get(index), Arrays.copyOfRange(remainingPath, 1, remainingPath.length));
            } else {
                return decompose(collection, remainingPath);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Object> decompose(List<Object> collection, String[] remainingPath) {
        Set<Object> items = new HashSet<>();

        for (Object item : collection) {
            Object result = recursiveGet(item, remainingPath);

            if (result != null) {
                if (result instanceof Iterable) {
                    List<Object> list = Iterables.toList((Iterable<Object>) result);
                    items.addAll(list);
                } else if (result.getClass().isArray()) {
                    List<Object> list = Arrays.asList(ObjectUtil.convertToObjectArray(result));
                    items.addAll(list);
                } else {
                    items.add(result);
                }
            }
        }
        return new ArrayList<>(items);
    }

    private int asInteger(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isEmbedded(String field) {
        return field.contains(FIELD_SEPARATOR);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeInt(size());
        for (Pair<String, Object> pair : this) {
            stream.writeObject(pair);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        int size = stream.readInt();
        for (int i = 0; i < size; i++) {
            Pair<String, Object> pair = (Pair<String, Object>) stream.readObject();
            super.put(pair.first(), pair.second());
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class MapPairIterator implements Iterator<Pair<String, Object>> {

        private final Iterator<Map.Entry<String, Object>> iterator;

        MapPairIterator(Iterator<Map.Entry<String, Object>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Pair<String, Object> next() {
            Map.Entry<String, Object> next = iterator.next();
            return new Pair<>(next.getKey(), next.getValue());
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

}
