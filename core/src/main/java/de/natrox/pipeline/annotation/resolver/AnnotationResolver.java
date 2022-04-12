package de.natrox.pipeline.annotation.resolver;

import com.google.common.base.Preconditions;
import de.natrox.pipeline.annotation.AutoSave;
import de.natrox.pipeline.annotation.CleanUp;
import de.natrox.pipeline.annotation.Preload;
import de.natrox.pipeline.annotation.Properties;
import de.natrox.pipeline.annotation.property.Context;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class AnnotationResolver {

    private AnnotationResolver() {
        throw new UnsupportedOperationException();
    }

    public static Properties properties(Class<?> classType) {
        Preconditions.checkNotNull(classType, "classType");
        var properties = classType.getAnnotation(Properties.class);
        if (properties == null)
            throw new RuntimeException(classType.getName() + " does not have @Properties Annotation set");
        return properties;
    }

    public static @NotNull String storageIdentifier(Class<?> classType) {
        Preconditions.checkNotNull(classType, "classType");
        return properties(classType).identifier();
    }

    public static @NotNull Context context(Class<?> classType) {
        Preconditions.checkNotNull(classType, "classType");
        return properties(classType).context();
    }

    public static @NotNull Optional<Preload> preload(Class<?> classType) {
        Preconditions.checkNotNull(classType, "classType");
        return Optional.ofNullable(classType.getAnnotation(Preload.class));
    }

    public static @NotNull Optional<AutoSave> autoSave(Class<?> classType) {
        Preconditions.checkNotNull(classType, "classType");
        return Optional.ofNullable(classType.getAnnotation(AutoSave.class));
    }

    public static @NotNull Optional<CleanUp> cleanUp(Class<?> classType) {
        Preconditions.checkNotNull(classType, "classType");
        return Optional.ofNullable(classType.getAnnotation(CleanUp.class));
    }
}
