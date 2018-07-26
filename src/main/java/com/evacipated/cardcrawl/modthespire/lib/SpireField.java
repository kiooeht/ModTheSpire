package com.evacipated.cardcrawl.modthespire.lib;

import java.lang.reflect.Field;

public class SpireField<T>
{
    public interface DefaultValue<T>
    {
        T get();
    }

    private DefaultValue<T> defaultValue;

    private Field field;

    @Deprecated
    public SpireField(T deprecated)
    {
        defaultValue = () -> deprecated;
    }

    public SpireField(DefaultValue<T> defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public void initialize(Class clz, String fieldName) throws NoSuchFieldException
    {
        field = clz.getDeclaredField(fieldName);
        field.setAccessible(true);
    }

    public T getDefaultValue()
    {
        return defaultValue.get();
    }

    public T get(Object __instance)
    {
        try {
            return (T) field.get(__instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void set(Object __intance, T value)
    {
        try {
            field.set(__intance, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
