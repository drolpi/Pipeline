package de.natrox.pipeline.json;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This interface is interesting to persistence data, of the implement object
 */
public interface Persistable {

    @NotNull Persistable write(@NotNull Writer writer);

    default @NotNull Persistable write(@NotNull OutputStream outputStream) {
        try (var writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            return this.write(writer);
        } catch (IOException exception) {
            return this;
        }
    }

    default @NotNull Persistable write(@Nullable Path path) {
        if (path != null) {
            // ensure that the parent directory exists
            try {
                Files.createDirectory(path.getParent());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            // write to the file
            try (var stream = Files.newOutputStream(path)) {
                return this.write(stream);
            } catch (IOException exception) {

            }
        }
        return this;
    }
}
