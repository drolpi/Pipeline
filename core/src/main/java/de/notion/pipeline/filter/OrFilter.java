package de.notion.pipeline.filter;

import java.util.Map;

public class OrFilter implements Filter {

    private final Filter first;
    private final Filter second;

    protected OrFilter(Filter first, Filter second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean check(Map<String, Object> data) {
        return first.check(data) || second.check(data);
    }
}
