package com.evacipated.cardcrawl.modthespire.lib;

import javassist.CtBehavior;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpireInsertPatch {
    Class<? extends SpireInsertLocator> locator() default NONE.class;
    int loc() default -1;
    int rloc() default -1;
    int[] locs() default {};
    int[] rlocs() default {};
    String[] localvars() default {};

    final class NONE extends SpireInsertLocator
    {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
        {
            return null;
        }
    }
}
