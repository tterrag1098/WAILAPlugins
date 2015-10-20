package tterrag.wailaplugins.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Plugin
{
    /**
     * The name of the plugin.
     * 
     * If this is not implemented, it will use the mod name of the <b>first</b> dependency.
     */
    String name() default "";

    /**
     * Mod ID dependencies of the plugin
     */
    String[] deps() default {};

    /**
     * Order for registration sorting. Higher numbers are registered later.
     */
    int order() default 0;
}
