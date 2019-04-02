package com.evacipated.cardcrawl.modthespire.patcher.javassist;

import com.evacipated.cardcrawl.modthespire.lib.SpireOverride;
import com.evacipated.cardcrawl.modthespire.patcher.javassist.convert.TransformSpecialCallVirtual;
import javassist.CannotCompileException;
import javassist.CodeConverter;
import javassist.CtMethod;
import javassist.Modifier;

import java.util.HashSet;
import java.util.Set;

public class MyCodeConverter extends CodeConverter
{
    private static Set<String> done = new HashSet<>();

    public static void reset()
    {
        done.clear();
    }

    public void redirectSpecialMethodCall(CtMethod origMethod) throws CannotCompileException
    {
        int mod1 = origMethod.getModifiers();
        if ((
                Modifier.isStatic(mod1)
                || !Modifier.isPrivate(mod1)
                || origMethod.getDeclaringClass().isInterface()
            )
            && !done.contains(origMethod.getLongName())
        ) {
            if (!Modifier.isPrivate(mod1) && !origMethod.hasAnnotation(SpireOverride.class)) {
                throw new CannotCompileException("invoke-type not special/private " + origMethod.getLongName());
            }
        }

        origMethod.setModifiers(Modifier.setProtected(origMethod.getModifiers()));
        done.add(origMethod.getLongName());

        transformers = new TransformSpecialCallVirtual(transformers, origMethod);
    }
}
