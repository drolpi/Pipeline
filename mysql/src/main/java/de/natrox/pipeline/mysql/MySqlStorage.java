package de.natrox.pipeline.mysql;

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.sql.SqlStorage;

public class MySqlStorage extends SqlStorage {

    protected final static Logger LOGGER = LogManager.logger(MySqlStorage.class);

    public MySqlStorage(Pipeline pipeline, HikariDataSource hikariDataSource) {
        super(pipeline, hikariDataSource);
        LOGGER.debug("MariaDB storage started");
    }
}
