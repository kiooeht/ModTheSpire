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
    public void doPatch() throws CannotCompileException, NotFoundException, ClassNotFoundException
    {
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
        if (ctMethodToPatch instanceof CtMethod
            && !returnType.equals(CtPrimitiveType.voidType)
            && returnType.equals(returnType.getClassPool().get(SpireReturn.class.getName()))) {

            funccall = SpireReturn.class.getName() + " opt = " + funccall + ");\n" +
                "if (opt.isPresent()) { return";
            if (!((CtMethod)ctMethodToPatch).getReturnType().equals(CtPrimitiveType.voidType)) {
                funccall += " (" + ((CtMethod) ctMethodToPatch).getReturnType().getName() + ")opt.get()";
            }
            funccall += "; }\n";
        } else {
            funccall += ");\n";
        }

        src += funccall;
        String src2 = src;
        src += postcallsrc + "}";
        src2 += postcallsrc2 + "}";

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
    }
}
