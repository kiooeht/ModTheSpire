package com.evacipated.cardcrawl.modthespire.lib;

import java.lang.annotation.*;

@Repeatable(SpirePatches2.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpirePatch2
{
    Class<?> clz() default void.class;
    String cls() default "";
    String method();
    Class<?>[] paramtypez() default {void.class};
    String[] paramtypes() default {"DEFAULT"};
    String requiredModId() default "";
    boolean optional() default false;
}
