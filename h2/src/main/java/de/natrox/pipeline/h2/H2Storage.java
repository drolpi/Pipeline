package de.natrox.pipeline.h2;

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.sql.SqlStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class H2Storage extends SqlStorage {

    private final static Logger LOGGER = LoggerFactory.getLogger(H2Storage.class);

    protected H2Storage(Pipeline pipeline, HikariDataSource hikariDataSource) {
        super(pipeline, hikariDataSource);

        LOGGER.debug("H2 storage initialized");
    }
}
