package de.natrox.pipeline.config.part;

public interface ConfigBuilder<T> {

    //TODO: Move into Common project as general IBuilder<T>

    T build();

}
