package de.notion.pipeline.operator.filter;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public record FieldFilter(@NotNull String fieldName, @NotNull Object obj) implements Filter {

    @Override
    public boolean check(@NotNull JsonObject data) {
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
