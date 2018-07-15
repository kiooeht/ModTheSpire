package com.evacipated.cardcrawl.modthespire.lib;

public class StaticSpireField<T> extends SpireField<T>
{
    public StaticSpireField(T defaultValue)
    {
        super(defaultValue);
    }

    public T get()
    {
        return super.get(null);
    }

    public void set(T value)
    {
        super.set(null, value);
    }
}
