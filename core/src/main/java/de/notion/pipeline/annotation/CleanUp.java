package de.notion.pipeline.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CleanUp {

    long time() default 20L;

    TimeUnit timeUnit() default TimeUnit.MINUTES;

    Action globalCacheAction() default Action.NONE;

    Action globalStorageAction() default Action.NONE;

}