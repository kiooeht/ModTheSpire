package com.evacipated.cardcrawl.modthespire.ui;

import java.util.Objects;

class UIScale
{
    private final float scale;

    UIScale(float scale)
    {
        this.scale = scale;
    }

    float getScale()
    {
        return scale;
    }

    @Override
    public String toString()
    {
        return String.format("%.0f%%", scale * 100);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UIScale uiScale = (UIScale) o;
        return Float.compare(scale, uiScale.scale) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(scale);
    }
}
