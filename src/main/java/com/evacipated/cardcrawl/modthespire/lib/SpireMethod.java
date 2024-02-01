package com.evacipated.cardcrawl.modthespire.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpireMethod
{
    Class<?> from();
    String methodName() default "";

    interface Helper<T, R>
    {
        R callSuper(Object... args);
        int timesSuperCalled();
        default boolean wasSuperCalled()
        {
            return timesSuperCalled() > 0;
        }

        T instance();
        boolean hasResult();
        R result();
    }
}
