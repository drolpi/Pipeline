package de.natrox.pipeline.part;

public interface PartProvider {

    boolean init() throws Exception;

    void shutdown();

}
