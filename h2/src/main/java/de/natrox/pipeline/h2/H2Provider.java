package de.natrox.pipeline.h2;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.io.FileUtil;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import org.h2.Driver;
import org.h2.jdbcx.JdbcDataSource;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public final class H2Provider implements GlobalStorageProvider {

    static {
        Driver.load();
    }

    private final Path path;
    private final HikariDataSource hikariDataSource;

    protected H2Provider(@NotNull H2Config config) throws Exception {
        Preconditions.checkNotNull(config, "config");
        this.path = Path.of(config.path());

        var parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            FileUtil.createDirectory(parent);
        }
        var hikariConfig = new HikariConfig();

        var dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:file:" + path.toAbsolutePath());
        hikariConfig.setDataSource(dataSource);

        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(100);
        hikariConfig.setConnectionTimeout(10_000);
        hikariConfig.setValidationTimeout(10_000);

        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void shutdown() {
        if (hikariDataSource != null) {
            hikariDataSource.close();
        }
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new H2Storage(pipeline, hikariDataSource);
    }
}
