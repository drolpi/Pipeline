package de.natrox.pipeline.mysql;

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.sql.SqlStorage;

final class MySqlStorage extends SqlStorage {

    protected final static Logger LOGGER = LogManager.logger(MySqlStorage.class);

    protected MySqlStorage(Pipeline pipeline, HikariDataSource hikariDataSource) {
        super(pipeline, hikariDataSource);

        LOGGER.debug("MySql storage initialized");
    }
}
