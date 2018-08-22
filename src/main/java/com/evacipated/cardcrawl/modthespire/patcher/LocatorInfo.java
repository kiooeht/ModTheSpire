package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import javassist.CtBehavior;

import java.lang.reflect.Constructor;

public class LocatorInfo {

    private CtBehavior ctMethodToPatch;
    private Class<?> finderClass;

    public LocatorInfo(CtBehavior ctMethodToPatch, Class<?> finderClass) {
        this.ctMethodToPatch = ctMethodToPatch;
        this.finderClass = finderClass;
    }

    public int[] findLines() throws Exception {
        Constructor<?> ctor = finderClass.getDeclaredConstructor();
        ctor.setAccessible(true);
        SpireInsertLocator obj = (SpireInsertLocator)ctor.newInstance();
        return obj.Locate(ctMethodToPatch);
    }

}
