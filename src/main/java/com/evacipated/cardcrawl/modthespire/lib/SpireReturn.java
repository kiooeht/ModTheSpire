package com.evacipated.cardcrawl.modthespire.lib;

import java.util.NoSuchElementException;

public final class SpireReturn<T>
{
    private static final SpireReturn<?> EMPTY = new SpireReturn<>();

    private final boolean hasValue;
    private final T value;

    private SpireReturn()
    {
        hasValue = false;
        value = null;
    }

    public static<T> SpireReturn<T> go()
    {
        @SuppressWarnings("unchecked")
        SpireReturn<T> t = (SpireReturn<T>) EMPTY;
        return t;
    }

    private SpireReturn(T value)
    {
        hasValue = true;
        this.value = value;
    }

    public static<T> SpireReturn<T> stop(T value)
    {
        return new SpireReturn<>(value);
    }

    public T get()
    {
        if (!isPresent()) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent()
    {
        return hasValue;
    }
}
