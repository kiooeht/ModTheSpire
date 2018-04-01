package com.evacipated.cardcrawl.modthespire.lib;

import java.lang.annotation.*;

@Repeatable(SpirePatches.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpirePatch {
    String cls();
    String method();
    String[] paramtypes() default {"DEFAULT"};
    boolean optional() default false;
}
