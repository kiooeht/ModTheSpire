package com.evacipated.cardcrawl.modthespire.patcher.javassist;

import com.evacipated.cardcrawl.modthespire.patcher.javassist.convert.TransformInsertGoto;
import com.evacipated.cardcrawl.modthespire.patcher.javassist.convert.TransformSpecialCallVirtual;
import javassist.*;

public class MyCodeConverter extends CodeConverter
{
    public void redirectSpecialMethodCall(CtMethod origMethod) throws CannotCompileException
    {
        int mod1 = origMethod.getModifiers();
        if (Modifier.isStatic(mod1)
            || !Modifier.isPrivate(mod1)
            || origMethod.getDeclaringClass().isInterface()) {
            throw new CannotCompileException("invoke-type not special/private " + origMethod.getLongName());
        }

        origMethod.setModifiers(Modifier.setProtected(origMethod.getModifiers()));

        transformers = new TransformSpecialCallVirtual(transformers, origMethod);
    }

    public void insertGoto(int fromLoc, int toLoc)
    {
        transformers = new TransformInsertGoto(transformers, fromLoc, toLoc);
    }
}
