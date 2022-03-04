package de.notion.pipeline.operator.filter;

import com.google.gson.JsonObject;

public record AndFilter(Filter first, Filter second) implements Filter {

    @Override
    public boolean check(JsonObject data) {
        return first.check(data) && second.check(data);
    }

}
