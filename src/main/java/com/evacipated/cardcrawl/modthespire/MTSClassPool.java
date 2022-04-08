package com.evacipated.cardcrawl.modthespire;

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MTSClassPool extends ClassPool
{
    private ClassLoader classLoader;
    private List<ClassPath> classPaths = new ArrayList<>();
    private Set<CtClass> outJar = null;

    public MTSClassPool(MTSClassLoader classLoader)
    {
        appendSystemPath();
        resetClassLoader(classLoader);
    }

    public void resetClassLoader(MTSClassLoader loader)
    {
        classLoader = loader;
        for (ClassPath classPath : classPaths) {
            removeClassPath(classPath);
        }
        classPaths.clear();

        insertClassPath(new LoaderClassPath(loader));
        loader.addStreamToClassPool(this);
    }

    @Override
    public ClassPath insertClassPath(ClassPath cp)
    {
        classPaths.add(cp);
        return super.insertClassPath(cp);
    }

    @Override
    public ClassPath appendClassPath(ClassPath cp)
    {
        classPaths.add(cp);
        return super.appendClassPath(cp);
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
        if (Loader.OUT_JAR || Loader.PACKAGE) {
            outJar = ret;
        }
        return ret;
    }

    public Set<CtClass> getOutJarClasses()
    {
        return outJar;
    }
}
