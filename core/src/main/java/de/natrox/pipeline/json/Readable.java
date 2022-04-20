package de.natrox.pipeline.json;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This interface is interesting to read data, of the implement object
 */
public interface Readable {

    @NotNull Readable read(@NotNull Reader reader);

    default @NotNull Readable read(@NotNull InputStream inputStream) {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return this.read(reader);
        } catch (IOException exception) {
            return this;
        }
    }

    default @NotNull Readable read(@Nullable Path path) {
        if (path != null && Files.exists(path)) {
            try (var inputStream = Files.newInputStream(path)) {
                return this.read(inputStream);
            } catch (IOException exception) {

            }
        }
        return this;
    }
}
