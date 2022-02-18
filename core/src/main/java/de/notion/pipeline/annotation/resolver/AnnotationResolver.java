package de.notion.pipeline.annotation.resolver;

import de.notion.pipeline.annotation.Context;
import de.notion.pipeline.annotation.Properties;
import de.notion.pipeline.annotation.auto.AutoCleanUp;
import de.notion.pipeline.annotation.auto.AutoLoad;
import de.notion.pipeline.annotation.auto.AutoSave;
import org.jetbrains.annotations.NotNull;

public class AnnotationResolver {

    //TODO: Return Optional<Type>

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
    public static AutoLoad autoLoad(Class<?> classType) {
        return classType.getAnnotation(AutoLoad.class);
    }

    @NotNull
    public static AutoSave autoSave(Class<?> classType) {
        return classType.getAnnotation(AutoSave.class);
    }

    @NotNull
    public static AutoCleanUp autoCleanUp(Class<?> classType) {
        return classType.getAnnotation(AutoCleanUp.class);
    }
}