package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import javassist.*;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

class ParamInfo2
{
    protected CtBehavior toPatch;
    protected CtMethod patchMethod;
    protected int toPatchParamPosition = -1;
    protected int patchParamPosition;
    protected String argName = null;
    protected String name = null;
    protected boolean isPrivate = false;
    protected String error = null;

    private static String findName(CtBehavior ctBehavior, int position)
    {
        MethodInfo methodInfo = ctBehavior.getMethodInfo2();
        LocalVariableAttribute table = (LocalVariableAttribute) methodInfo.getCodeAttribute().getAttribute(LocalVariableAttribute.tag);
        if (table != null) {
            int j = 0;
            for (int i=0; i<table.tableLength(); ++i) {
                if (table.startPc(i) == 0) {
                    if (j == position) {
                        return table.variableName(i);
                    }
                    ++j;
                }
            }
        }
        return null;
    }

    ParamInfo2(CtBehavior toPatch, CtMethod patchMethod, int position) throws PatchingException
    {
        this.toPatch = toPatch;
        this.patchMethod = patchMethod;
        patchParamPosition = position;

        try {
            String patchParamName = findName(patchMethod, patchParamPosition);
            if (patchParamName != null) {
                if (patchParamName.equals("__instance")) {
                    if (Modifier.isStatic(toPatch.getModifiers())) {
                        error = "Cannot have __instance parameter for static method";
                    } else {
                        name = patchParamName;
                        argName = "$0";
                        toPatchParamPosition = 0;
                    }
                    return;
                } else if (patchParamName.equals("__args")) {
                    name = patchParamName;
                    argName = "$args";
                    return;
                }

                if (specialNameCheck(patchParamName)) {
                    return;
                }

                CtClass[] toPatchParamTypes = toPatch.getParameterTypes();
                boolean isStatic = Modifier.isStatic(toPatch.getModifiers());
                for (int i = 1; i < toPatchParamTypes.length + 1; ++i) {
                    String toPatchParamName = findName(toPatch, i + (isStatic ? -1 : 0));
                    if (patchParamName.equals(toPatchParamName)) {
                        name = patchParamName;
                        argName = "$" + i;
                        toPatchParamPosition = i;
                        break;
                    }
                }

                if (argName == null && patchParamName.startsWith("___")) {
                    isPrivate = true;
                    name = patchParamName;
                    argName = patchParamName.replaceFirst("___", "");
                }

                if (name == null) {
                    error = String.format("No matching parameter with name \"%s\"", patchParamName);
                }
            }
        } catch (NotFoundException e) {
            throw new PatchingException(e);
        }
    }

    // for overriding
    protected boolean specialNameCheck(String patchParamName) throws PatchingException
    {
        return false;
    }

    boolean isPrivateCapture()
    {
        return isPrivate;
    }

    CtClass getPrivateCaptureType() throws PatchingException
    {
        if (!isPrivateCapture()) {
            throw new PatchingException("Not a private capture, this shouldn't have been called");
        }

        CtClass declaringClass = toPatch.getDeclaringClass();
        try {
            return declaringClass.getDeclaredField(getName()).getType();
        } catch (NotFoundException e) {
            return null;
        }
    }

    CtClass getDestByRefType() throws ClassNotFoundException
    {
        String typename = null;
        for (Object o : getAnnotations()) {
            if (o instanceof ByRef && !((ByRef) o).type().isEmpty()) {
                typename = ((ByRef) o).type();
            }
        }

        try {
            return toPatch.getDeclaringClass().getClassPool().get(typename);
        } catch (NotFoundException e) {
            return null;
        }
    }

    boolean hasError()
    {
        return error != null;
    }

    String getError()
    {
        return error;
    }

    int getParamCount()
    {
        return patchMethod.getAvailableParameterAnnotations().length;
    }

    String getArgName()
    {
        return argName;
    }

    int getPatchParamPosition()
    {
        return patchParamPosition;
    }

    String getName()
    {
        return name;
    }

    CtClass getType() throws NotFoundException
    {
        if (toPatchParamPosition < 0) {
            return null;
        }
        if (toPatchParamPosition == 0) {
            return toPatch.getDeclaringClass();
        }
        return toPatch.getParameterTypes()[toPatchParamPosition - 1];
    }

    String getTypename() throws NotFoundException
    {
        CtClass type = getType();
        if (type == null) {
            return "";
        }
        return type.getName();
    }

    CtClass getPatchParamType() throws NotFoundException
    {
        if (patchParamPosition < 0) {
            return null;
        }
        return patchMethod.getParameterTypes()[patchParamPosition];
    }

    String getPatchParamTypename() throws NotFoundException
    {
        CtClass type = getPatchParamType();
        if (type == null) {
            return "";
        }
        return type.getName();
    }

    Object[] getAnnotations() throws ClassNotFoundException
    {
        Object[][] o = patchMethod.getParameterAnnotations();
        return o[patchParamPosition];
    }
}
