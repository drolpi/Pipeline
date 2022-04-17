package de.natrox.pipeline.mysql;

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.sql.SqlStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MySqlStorage extends SqlStorage {

    protected final static Logger LOGGER = LoggerFactory.getLogger(MySqlStorage.class);

    protected MySqlStorage(Pipeline pipeline, HikariDataSource hikariDataSource) {
        super(pipeline, hikariDataSource);

        LOGGER.debug("MySql storage initialized");
    }
}
