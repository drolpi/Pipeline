package de.natrox.pipeline.operator.filter;

import de.natrox.pipeline.json.document.JsonDocument;
import org.jetbrains.annotations.NotNull;

public record FieldFilter(@NotNull String fieldName, @NotNull Object obj) implements Filter {

    @Override
    public boolean check(@NotNull JsonDocument data) {
        for (var key : data.keys()) {
            if (key == null)
                continue;
            var value = data.get(key);
            if (value == null)
                continue;

            if (key.equals(fieldName) && value.equals(obj)) {
                return true;
            }
        }
        return false;
    }
}
