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

    public SpireField(DefaultValue<T> defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public SpireField(SpireField<T> originalSpireField)
    {
        if (originalSpireField != null) {
            defaultValue = originalSpireField.defaultValue;
        }
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
        // This should never be called, but serves as a fallback
        try {
            return (T) field.get(__instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void set(Object __instance, T value)
    {
        // This should never be called, but serves as a fallback
        try {
            field.set(__instance, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
