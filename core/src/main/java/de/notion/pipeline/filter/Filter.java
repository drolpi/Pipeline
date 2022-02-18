package de.notion.pipeline.filter;

import java.util.Map;

public interface Filter {

    boolean check(Map<String, Object> data);

}
