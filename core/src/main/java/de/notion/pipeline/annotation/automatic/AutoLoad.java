package de.notion.pipeline.annotation.automatic;

import de.notion.pipeline.Pipeline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoLoad {

    Pipeline.QueryStrategy[] creationStrategies() default {Pipeline.QueryStrategy.ALL};

}
