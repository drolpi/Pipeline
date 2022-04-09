package de.natrox.pipeline.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class HikariUtil {

    private static final String CONNECT_URL_FORMAT = "jdbc:mysql://%s:%d/%s?serverTimezone=UTC&useSSL=%b&trustServerCertificate=%b";

    private HikariUtil() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull HikariDataSource createDataSource(Consumer<HikariConfig> consumer) {
        var config = new HikariConfig();

        config.setMinimumIdle(2);
        config.setMaximumPoolSize(100);
        config.setConnectionTimeout(10_000);
        config.setValidationTimeout(10_000);

        consumer.accept(config);

        return new HikariDataSource(config);
    }

}
