package com.evacipated.cardcrawl.modthespire.lib;

import java.lang.annotation.*;

@Repeatable(SpirePatches.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpirePatch
{
    Class<?> clz() default void.class;
    String cls() default "";
    String method();
    Class<?>[] paramtypez() default {void.class};
    String[] paramtypes() default {"DEFAULT"};
    String requiredModId() default "";
    boolean optional() default false;

    String CONSTRUCTOR = "<ctor>";
    String STATICINITIALIZER = "<staticinit>";
    String CLASS = "<class>";
}
