package de.notion.pipeline.annotation.resolver;

import de.notion.pipeline.annotation.CleanUp;
import de.notion.pipeline.annotation.Context;
import de.notion.pipeline.annotation.Preload;
import de.notion.pipeline.annotation.Properties;
import de.notion.pipeline.annotation.Unload;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class AnnotationResolver {

    @NotNull
    public static String storageIdentifier(Class<?> classType) {
        var properties = classType.getAnnotation(Properties.class);
        if (properties == null)
            throw new RuntimeException(classType.getName() + " does not have @Properties Annotation set");
        return properties.identifier();
    }

    @NotNull
    public static Context context(Class<?> classType) {
        var properties = classType.getAnnotation(Properties.class);
        if (properties == null)
            throw new RuntimeException(classType.getName() + " does not have @Properties Annotation set");
        return properties.context();
    }

    @NotNull
    public static Optional<Preload> preload(Class<?> classType) {
        return Optional.ofNullable(classType.getAnnotation(Preload.class));
    }

    @NotNull
    public static Optional<Unload> autoSave(Class<?> classType) {
        return Optional.ofNullable(classType.getAnnotation(Unload.class));
    }

    @NotNull
    public static Optional<CleanUp> cleanUp(Class<?> classType) {
        return Optional.ofNullable(classType.getAnnotation(CleanUp.class));
    }
}