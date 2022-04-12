package de.natrox.pipeline.operator.filter;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public final class Filters {

    private Filters() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public static Filter or(@NotNull Filter first, @NotNull Filter second) {
        Preconditions.checkNotNull(first, "first Filter");
        Preconditions.checkNotNull(second, "second Filter");
        return new OrFilter(first, second);
    }

    @NotNull
    public static Filter and(@NotNull Filter first, @NotNull Filter second) {
        Preconditions.checkNotNull(first, "first Filter");
        Preconditions.checkNotNull(second, "second Filter");
        return new AndFilter(first, second);
    }

    @NotNull
    public static Filter field(@NotNull String fieldName, @NotNull Object value) {
        Preconditions.checkNotNull(fieldName, "fieldName");
        Preconditions.checkNotNull(value, "value");
        return new FieldFilter(fieldName, value);
    }
}
