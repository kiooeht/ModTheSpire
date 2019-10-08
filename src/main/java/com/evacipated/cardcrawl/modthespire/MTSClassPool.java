package com.evacipated.cardcrawl.modthespire;

import javassist.ClassPool;
import javassist.CtClass;

import java.util.HashSet;
import java.util.Set;

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

    public Set<CtClass> getModifiedClasses()
    {
        Set<CtClass> ret = new HashSet<>();
        for (Object v : classes.values()) {
            CtClass cls = (CtClass) v;
            if (cls.isModified()) {
                ret.add(cls);
            }
        }
        return ret;
    }
}
