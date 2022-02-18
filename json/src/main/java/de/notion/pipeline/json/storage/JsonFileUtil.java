package de.notion.pipeline.json.storage;

import de.notion.common.io.FileUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonFileUtil {

    public static String loadFromJson(Path source) {
        if (source == null) {
            throw new IllegalArgumentException("The provided path can not be null.");
        }

        try (var reader = new BufferedReader(new FileReader(source.toFile()))) {
            var buffer = new StringBuilder();
            while (true) {
                String line;
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (line == null) {
                    break;
                } else {
                    buffer.append(line);
                    buffer.append("\n");
                }
            }
            reader.close();
            return buffer.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public static void saveToJson(String object, Path externalPath) {
        if (object == null) {
            throw new IllegalArgumentException("The provided object can not be null.");
        }

        if (externalPath == null) {
            throw new IllegalArgumentException("The provided path can not be null.");
        }

        var parent = externalPath.getParent();

        if (!Files.exists(externalPath)) {
            if (parent != null && !Files.exists(parent)) {
                FileUtil.createDirectory(parent);
            }
        }

        try (var writer = new FileWriter(externalPath.toFile())) {
            writer.write(object);
        } catch (IOException ignored) {
        }
    }

}
