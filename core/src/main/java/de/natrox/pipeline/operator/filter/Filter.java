package de.natrox.pipeline.operator.filter;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public interface Filter {

    boolean check(@NotNull JsonObject data);

}
