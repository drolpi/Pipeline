package de.notion.pipeline.filter;

import java.util.Objects;

public class Filters {

    //TODO: null checks

    public static Filter or(Filter first, Filter second) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        return new OrFilter(first, second);
    }

    public static Filter and(Filter first, Filter second) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        return new AndFilter(first, second);
    }

    public static Filter field(String fieldName, Object obj) {
        Objects.requireNonNull(fieldName, "fieldName can't be null");
        Objects.requireNonNull(obj, "obj can't be null");
        return new FieldFilter(fieldName, obj);
    }
}
