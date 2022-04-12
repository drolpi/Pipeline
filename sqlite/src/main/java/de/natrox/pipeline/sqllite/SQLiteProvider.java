package de.natrox.pipeline.sqllite;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.io.FileUtil;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqlite.SQLiteDataSource;

import java.nio.file.Files;
import java.nio.file.Path;

public final class SQLiteProvider implements GlobalStorageProvider {

    private final Path path;
    private @Nullable HikariDataSource hikariDataSource;

    protected SQLiteProvider(@NotNull SQLiteConfig config) {
        Preconditions.checkNotNull(config, "config");
        this.path = Path.of(config.path());
    }

    @Override
    public boolean init() throws Exception {
        var parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            FileUtil.createDirectory(parent);
        }
        var config = new HikariConfig();

        var dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + path.toAbsolutePath());
        config.setDataSource(dataSource);

        config.setMinimumIdle(2);
        config.setMaximumPoolSize(100);
        config.setConnectionTimeout(10_000);
        config.setValidationTimeout(10_000);

        this.hikariDataSource = new HikariDataSource(config);
        return true;
    }

    @Override
    public void shutdown() {
        if (hikariDataSource != null) {
            hikariDataSource.close();
        }
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new SQLiteStorage(pipeline, hikariDataSource);
    }
}
