package de.notion.pipeline.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Unload {

    Action globalCacheAction() default Action.NONE;

    Action globalStorageAction() default Action.NONE;

}
