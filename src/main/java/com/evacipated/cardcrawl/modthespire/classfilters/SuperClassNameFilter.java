package com.evacipated.cardcrawl.modthespire.classfilters;

import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;

public class SuperClassNameFilter implements ClassFilter
{
    private final String baseClassName;

    public SuperClassNameFilter(Class baseClass)
    {
        this(baseClass.getName());
    }

    public SuperClassNameFilter(String baseClassName)
    {
        this.baseClassName = baseClassName;
    }

    @Override
    public boolean accept(ClassInfo classInfo, ClassFinder classFinder)
    {
        return baseClassName != null && baseClassName.equals(classInfo.getSuperClassName());
    }
}
