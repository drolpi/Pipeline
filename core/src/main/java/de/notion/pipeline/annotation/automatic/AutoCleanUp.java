package de.notion.pipeline.annotation.automatic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoCleanUp {

    boolean saveToGlobalStorage() default true;

    long time() default 20L;

    TimeUnit timeUnit() default TimeUnit.MINUTES;

}
