package de.natrox.pipeline.annotation.resolver;

import de.natrox.pipeline.annotation.AutoSave;
import de.natrox.pipeline.annotation.CleanUp;
import de.natrox.pipeline.annotation.Preload;
import de.natrox.pipeline.annotation.Properties;
import de.natrox.pipeline.annotation.property.Context;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class AnnotationResolver {

    public static Properties properties(Class<?> classType) {
        var properties = classType.getAnnotation(Properties.class);
        if (properties == null)
            throw new RuntimeException(classType.getName() + " does not have @Properties Annotation set");
        return properties;
    }

    @NotNull
    public static String storageIdentifier(Class<?> classType) {
        return properties(classType).identifier();
    }

    @NotNull
    public static Context context(Class<?> classType) {
        return properties(classType).context();
    }

    @NotNull
    public static Optional<Preload> preload(Class<?> classType) {
        return Optional.ofNullable(classType.getAnnotation(Preload.class));
    }

    @NotNull
    public static Optional<AutoSave> autoSave(Class<?> classType) {
        return Optional.ofNullable(classType.getAnnotation(AutoSave.class));
    }

    @NotNull
    public static Optional<CleanUp> cleanUp(Class<?> classType) {
        return Optional.ofNullable(classType.getAnnotation(CleanUp.class));
    }
}
