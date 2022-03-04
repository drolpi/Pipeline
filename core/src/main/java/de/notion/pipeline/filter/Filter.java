package de.notion.pipeline.filter;

import com.google.gson.JsonObject;

public interface Filter {

    boolean check(JsonObject data);

}
