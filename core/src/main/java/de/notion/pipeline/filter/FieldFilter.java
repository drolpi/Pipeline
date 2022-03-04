package de.notion.pipeline.filter;

import com.google.gson.JsonObject;

public record FieldFilter(String fieldName, Object obj) implements Filter {

    @Override
    public boolean check(JsonObject data) {
        for (var entry : data.entrySet()) {
            if (entry.getKey() == null)
                continue;

            if (entry.getValue() == null)
                continue;

            if (entry.getKey().equals(fieldName) && entry.getValue().equals(obj)) {
                return true;
            }
        }
        return false;
    }
}
