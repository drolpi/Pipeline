package de.notion.pipeline.operator.filter;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public record AndFilter(@NotNull Filter first, @NotNull Filter second) implements Filter {

    @Override
    public boolean check(@NotNull JsonObject data) {
        return first.check(data) && second.check(data);
    }

}
