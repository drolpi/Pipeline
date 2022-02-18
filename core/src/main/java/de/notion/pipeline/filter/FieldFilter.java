package de.notion.pipeline.filter;

import java.util.Map;

public class FieldFilter implements Filter {

    private final String fieldName;
    private final Object obj;

    protected FieldFilter(String fieldName, Object obj) {
        this.fieldName = fieldName;
        this.obj = obj;
    }

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
