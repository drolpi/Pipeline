package de.natrox.pipeline.sqllite;

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.sql.SqlStorage;

final class SQLiteStorage extends SqlStorage {

    private final static Logger LOGGER = LogManager.logger(SQLiteStorage.class);

    protected SQLiteStorage(Pipeline pipeline, HikariDataSource hikariDataSource) {
        super(pipeline, hikariDataSource);

        LOGGER.debug("SqlLite storage started");
    }
}
