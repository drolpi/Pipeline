package de.natrox.pipeline.sqllite;

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.sql.SqlStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SQLiteStorage extends SqlStorage {

    private final static Logger LOGGER = LoggerFactory.getLogger(SQLiteStorage.class);

    protected SQLiteStorage(Pipeline pipeline, HikariDataSource hikariDataSource) {
        super(pipeline, hikariDataSource);

        LOGGER.debug("SqlLite storage started");
    }
}
