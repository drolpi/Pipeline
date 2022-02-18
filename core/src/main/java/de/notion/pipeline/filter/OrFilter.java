package de.notion.pipeline.filter;

import java.util.Map;

public record OrFilter(Filter first,
                       Filter second) implements Filter {

    @Override
    public boolean check(Map<String, Object> data) {
        return first.check(data) || second.check(data);
    }
}
