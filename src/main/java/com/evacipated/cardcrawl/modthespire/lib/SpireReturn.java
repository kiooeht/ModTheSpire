package com.evacipated.cardcrawl.modthespire.lib;

import java.util.NoSuchElementException;

public final class SpireReturn<T>
{
    private static final SpireReturn<?> EMPTY = new SpireReturn<>();
    private static final SpireReturn<Object> PLACEHOLDER = new SpireReturn<>(null);

    private final boolean hasValue;
    private T value;

    private SpireReturn()
    {
        hasValue = false;
        value = null;
    }

    public static<T> SpireReturn<T> Continue()
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

    public static<T> SpireReturn<T> Return(T value)
    {
        PLACEHOLDER.value = value;
        @SuppressWarnings("unchecked")
        SpireReturn<T> ret = (SpireReturn<T>) PLACEHOLDER;
        return ret;
    }

    public static SpireReturn<Void> Return()
    {
        return Return(null);
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
