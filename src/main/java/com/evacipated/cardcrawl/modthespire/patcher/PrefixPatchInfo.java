package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import javassist.*;

public class PrefixPatchInfo extends PatchInfo
{
    public PrefixPatchInfo(CtBehavior ctMethodToPatch, CtMethod patchMethod)
    {
        super(ctMethodToPatch, patchMethod);
    }

    @Override
    protected String debugMsg()
    {
        return "Adding Prefix...";
    }

    @Override
    public int patchOrdering()
    {
        return 1;
    }

    @Override
    public void doPatch() throws PatchingException
    {
        try {
            String src = "{\n";
            String funccall = patchMethod.getDeclaringClass().getName() + "." + patchMethod.getName() + "(";
            String postcallsrc = "";
            String postcallsrc2 = "";

            int paramOffset = (Modifier.isStatic(ctMethodToPatch.getModifiers()) ? 1 : 0);
            CtClass[] prefixParamTypes = patchMethod.getParameterTypes();
            Object[][] prefixParamAnnotations = patchMethod.getParameterAnnotations();
            for (int i = 0; i < prefixParamTypes.length; ++i) {
                if (paramByRef(prefixParamAnnotations[i])) {
                    src += prefixParamTypes[i].getName() + " __param" + i + " = new " + prefixParamTypes[i].getName() + "{" + "$" + (i + paramOffset) + "};\n";
                    funccall += "__param" + i;

                    postcallsrc += "$" + (i + paramOffset) + " = ";
                    postcallsrc2 += "$" + (i + paramOffset) + " = ";

                    String typename = paramByRefTypename2(ctMethodToPatch, i);
                    if (!typename.isEmpty()) {
                        postcallsrc += "(" + typename + ")";
                        postcallsrc2 += "(com.megacrit.cardcrawl." + typename + ")";
                    }
                    postcallsrc += "__param" + i + "[0];\n";
                    postcallsrc2 += "__param" + i + "[0];\n";
                } else {
                    funccall += "$" + (i + paramOffset);
                }
                if (i < prefixParamTypes.length - 1) {
                    funccall += ", ";
                }
            }


            CtClass returnType = patchMethod.getReturnType();
            boolean hasEarlyReturn = false;
            if (ctMethodToPatch instanceof CtMethod
                && !returnType.equals(CtPrimitiveType.voidType)
                && returnType.equals(returnType.getClassPool().get(SpireReturn.class.getName()))) {

                hasEarlyReturn = true;
            } else if (ctMethodToPatch instanceof CtConstructor
                && !returnType.equals(CtPrimitiveType.voidType)
                && returnType.equals(returnType.getClassPool().get(SpireReturn.class.getName()))) {

                hasEarlyReturn = true;
            }

            if (hasEarlyReturn) {
                funccall = SpireReturn.class.getName() + " opt = " + funccall + ");\n";
            } else {
                funccall += ");\n";
            }

            src += funccall;
            String src2 = src;
            src += postcallsrc;
            src2 += postcallsrc2;

            if (hasEarlyReturn) {
                String earlyReturn = "if (opt.isPresent()) { return";
                if (ctMethodToPatch instanceof CtMethod && !((CtMethod) ctMethodToPatch).getReturnType().equals(CtPrimitiveType.voidType)) {
                    CtClass toPatchReturnType = ((CtMethod) ctMethodToPatch).getReturnType();
                    String toPatchReturnTypeName = toPatchReturnType.getName();
                    if (toPatchReturnType.isPrimitive()) {
                        if (toPatchReturnType.equals(CtPrimitiveType.intType)) {
                            toPatchReturnTypeName = "Integer";
                        } else if (toPatchReturnType.equals(CtPrimitiveType.charType)) {
                            toPatchReturnTypeName = "Character";
                        } else {
                            toPatchReturnTypeName = toPatchReturnTypeName.substring(0, 1).toUpperCase() + toPatchReturnTypeName.substring(1);
                        }
                        earlyReturn += " (";
                    }
                    earlyReturn += " (" + toPatchReturnTypeName + ")opt.get()";
                    if (toPatchReturnType.isPrimitive()) {
                        earlyReturn += ")." + toPatchReturnType.getName() + "Value()";
                    }
                }
                earlyReturn += "; }\n";

                src += earlyReturn;
                src2 += earlyReturn;
            }

            src += "}";
            src2 += "}";

            if (Loader.DEBUG) {
                System.out.println(src);
            }
            try {
                if (ctMethodToPatch instanceof CtConstructor && !((CtConstructor) ctMethodToPatch).isClassInitializer()) {
                    ((CtConstructor) ctMethodToPatch).insertBeforeBody(src);
                } else {
                    ctMethodToPatch.insertBefore(src);
                }
            } catch (CannotCompileException e) {
                try {
                    if (ctMethodToPatch instanceof CtConstructor) {
                        ((CtConstructor) ctMethodToPatch).insertBeforeBody(src2);
                    } else {
                        ctMethodToPatch.insertBefore(src2);
                    }
                } catch (CannotCompileException e2) {
                    throw e;
                }
            }
        } catch (CannotCompileException | ClassNotFoundException | NotFoundException e) {
            throw new PatchingException(e);
        }
    }
}
