package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

class ParamInfo
{
    private CtBehavior ctBehavior;
    private int position;
    private String name;
    private boolean isPrivate = false;

    private static String findName(CtBehavior ctBehavior, int position)
    {
        MethodInfo methodInfo = ctBehavior.getMethodInfo2();
        LocalVariableAttribute table = (LocalVariableAttribute) methodInfo.getCodeAttribute().getAttribute(LocalVariableAttribute.tag);
        if (table != null) {
            int j = 0;
            for (int i=0; i<table.tableLength(); ++i) {
                if (table.startPc(i) == 0) {
                    if (j == 0 && !table.variableName(i).equals("this")) {
                        ++j; // Skip to position 1 if `this` doesn't exist (static method)
                    }
                    if (j == position) {
                        return table.variableName(i);
                    }
                    ++j;
                }
            }
        }
        return null;
    }

    ParamInfo(CtBehavior ctBehavior, int position)
    {
        this.ctBehavior = ctBehavior;
        if (Modifier.isStatic(ctBehavior.getModifiers())) {
            ++position;
        }
        this.position = position;
        name = findName(ctBehavior, position);

        try {
            if (this.position > ctBehavior.getParameterTypes().length) {
                this.position = -1;
                name = null;
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        if (name != null) {
            if (name.startsWith("___")) {
                isPrivate = true;
                name = name.replaceFirst("___", "");
            }
        }
    }

    boolean isPrivateCapture()
    {
        return isPrivate;
    }

    CtClass getPrivateCaptureType(ParamInfo destInfo) throws PatchingException
    {
        if (!destInfo.isPrivateCapture()) {
            throw new PatchingException("Not a private capture, this shouldn't have been called");
        }

        CtClass declaringClass = ctBehavior.getDeclaringClass();
        try {
            return declaringClass.getDeclaredField(destInfo.getName()).getType();
        } catch (NotFoundException e) {
            return null;
        }
    }

    CtClass getDestByRefType(ParamInfo destInfo) throws ClassNotFoundException
    {
        String typename = null;
        for (Object o : destInfo.getAnnotations()) {
            if (o instanceof ByRef && !((ByRef) o).type().isEmpty()) {
                typename = ((ByRef) o).type();
            }
        }

        try {
            return ctBehavior.getDeclaringClass().getClassPool().get(typename);
        } catch (NotFoundException e) {
            return null;
        }
    }

    int getParamCount()
    {
        return ctBehavior.getAvailableParameterAnnotations().length;
    }

    int getPosition()
    {
        return position;
    }

    String getName()
    {
        return name;
    }

    CtClass getType() throws NotFoundException
    {
        if (position < 0) {
            return null;
        }
        if (position == 0) {
            return ctBehavior.getDeclaringClass();
        }
        return ctBehavior.getParameterTypes()[position - 1];
    }

    String getTypename() throws NotFoundException
    {
        CtClass type = getType();
        if (type == null) {
            return "";
        }
        return type.getName();
    }

    Object[] getAnnotations() throws ClassNotFoundException
    {
        return ctBehavior.getParameterAnnotations()[position - 1];
    }
}
