package de.natrox.pipeline.sqllite;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.io.FileUtil;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import org.jetbrains.annotations.NotNull;
import org.sqlite.SQLiteDataSource;

import java.nio.file.Files;
import java.nio.file.Path;

public final class SQLiteProvider implements GlobalStorageProvider {

    private final HikariDataSource hikariDataSource;

    protected SQLiteProvider(@NotNull SQLiteConfig config) throws Exception {
        Preconditions.checkNotNull(config, "config");
        var path = Path.of(config.path());
        var parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            FileUtil.createDirectory(parent);
        }
        var hikariConfig = new HikariConfig();

        var dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + path.toAbsolutePath());
        hikariConfig.setDataSource(dataSource);

        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(100);
        hikariConfig.setConnectionTimeout(10_000);
        hikariConfig.setValidationTimeout(10_000);

        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void shutdown() {
        hikariDataSource.close();
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new SQLiteStorage(pipeline, hikariDataSource);
    }
}
