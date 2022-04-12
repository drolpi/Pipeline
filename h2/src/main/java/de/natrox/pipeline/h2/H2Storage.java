package de.natrox.pipeline.h2;

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.sql.SqlStorage;

final class H2Storage extends SqlStorage {

    private final static Logger LOGGER = LogManager.logger(H2Storage.class);

    protected H2Storage(Pipeline pipeline, HikariDataSource hikariDataSource) {
        super(pipeline, hikariDataSource);

        LOGGER.debug("H2 storage initialized");
    }
}
