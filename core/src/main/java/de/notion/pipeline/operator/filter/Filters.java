package de.notion.pipeline.operator.filter;

import java.util.Objects;

public class Filters {

    public static Filter or(Filter first, Filter second) {
        Objects.requireNonNull(first, "first Filter can't be null");
        Objects.requireNonNull(second, "second Filter can't be null");
        return new OrFilter(first, second);
    }

    public static Filter and(Filter first, Filter second) {
        Objects.requireNonNull(first, "first Filter can't be null");
        Objects.requireNonNull(second, "second Filter can't be null");
        return new AndFilter(first, second);
    }

    public static Filter field(String fieldName, Object obj) {
        Objects.requireNonNull(fieldName, "fieldName can't be null");
        Objects.requireNonNull(obj, "obj can't be null");
        return new FieldFilter(fieldName, obj);
    }
}
