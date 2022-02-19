package de.notion.pipeline.annotation.automatic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoSave {

    boolean deleteFromGlobalCache() default false;

    boolean saveToGlobalStorage() default true;

}
