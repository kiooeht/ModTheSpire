package com.evacipated.cardcrawl.modthespire.lib;

public class StaticSpireField<T> extends SpireField<T>
{
    public StaticSpireField(DefaultValue<T> defaultValue)
    {
        super(defaultValue);
    }

    public StaticSpireField(SpireField<T> originalSpireField)
    {
        super(originalSpireField);
    }

    public T get()
    {
        return get(null);
    }

    public void set(T value)
    {
        set(null, value);
    }
}
