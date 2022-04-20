package de.natrox.pipeline.operator.filter;

import de.natrox.pipeline.json.document.JsonDocument;
import org.jetbrains.annotations.NotNull;

public interface Filter {

    boolean check(@NotNull JsonDocument data);

}
