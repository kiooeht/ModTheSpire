package com.evacipated.cardcrawl.modthespire;

import javassist.ClassPool;

public class MTSClassPool extends ClassPool
{
    private ClassLoader classLoader;

    public MTSClassPool(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
        appendSystemPath();
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    public void setParent(ClassPool parent)
    {
        this.parent = parent;
    }
}
