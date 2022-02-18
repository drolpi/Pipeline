package de.notion.pipeline.filter;

import java.util.Map;

public record FieldFilter(String fieldName, Object obj) implements Filter {

    @Override
    public boolean check(Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getKey() == null)
                continue;

            if (entry.getValue() == null)
                continue;

            if (entry.getKey().equals(fieldName) && entry.getValue().toString().equals(obj.toString())) {
                return true;
            }
        }
        return false;
    }
}
