package de.notion.pipeline.operator.filter;

import com.google.gson.JsonObject;

public interface Filter {

    boolean check(JsonObject data);

}
