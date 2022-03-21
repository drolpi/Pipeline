package de.natrox.pipeline.operator;

import de.natrox.pipeline.operator.filter.Filter;
import org.jetbrains.annotations.Nullable;

public final class FindOptions {

    private Filter filter;
    private Object sort;
    private int limit;
    private int skip;

    public FindOptions() {
        this.limit = -1;
        this.skip = -1;
    }

    @Nullable
    public Filter filter() {
        return this.filter;
    }

    public void setFilter(@Nullable Filter filter) {
        this.filter = filter;
    }

    @Nullable
    public Object sort() {
        return this.sort;
    }

    public void setSort(@Nullable Object sort) {
        this.sort = sort;
    }

    public int limit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int skip() {
        return this.skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }
}
