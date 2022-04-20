package de.natrox.pipeline.operator.filter;

import de.natrox.pipeline.json.gson.JsonDocument;
import org.jetbrains.annotations.NotNull;

public record AndFilter(@NotNull Filter first, @NotNull Filter second) implements Filter {

    @Override
    public boolean check(@NotNull JsonDocument data) {
        return first.check(data) && second.check(data);
    }

}
