package de.notion.pipeline.operator.filter;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Filters {

    private Filters() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public static Filter or(@NotNull Filter first, @NotNull Filter second) {
        Objects.requireNonNull(first, "first Filter can't be null");
        Objects.requireNonNull(second, "second Filter can't be null");
        return new OrFilter(first, second);
    }

    @NotNull
    public static Filter and(@NotNull Filter first, @NotNull Filter second) {
        Objects.requireNonNull(first, "first Filter can't be null");
        Objects.requireNonNull(second, "second Filter can't be null");
        return new AndFilter(first, second);
    }

    @NotNull
    public static Filter field(@NotNull String fieldName, @NotNull Object obj) {
        Objects.requireNonNull(fieldName, "fieldName can't be null");
        Objects.requireNonNull(obj, "obj can't be null");
        return new FieldFilter(fieldName, obj);
    }
}
