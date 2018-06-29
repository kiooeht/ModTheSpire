package com.evacipated.cardcrawl.modthespire;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;

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
