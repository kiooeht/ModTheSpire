package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import javassist.*;

public class InsertPatchInfo extends PatchInfo
{
    protected SpireInsertPatch info;
    protected int loc;

    public InsertPatchInfo(SpireInsertPatch info, int loc, CtBehavior ctMethodToPatch, CtMethod patchMethod)
    {
        super(ctMethodToPatch, patchMethod);
        this.info = info;
        this.loc = loc;
    }

    @Override
    protected String debugMsg()
    {
        if (info.loc() >= 0) {
            return "Adding Insert @ " + loc + "...";
        } else {
            return "Adding Insert @ r" + info.rloc() + " (abs:" + loc + ")...";
        }
    }

    @Override
    public int patchOrdering()
    {
        return -2;
    }

    @Override
    public void doPatch() throws NotFoundException, ClassNotFoundException, CannotCompileException
    {
        CtClass[] insertParamTypes = patchMethod.getParameterTypes();
        Object[][] insertParamAnnotations = patchMethod.getParameterAnnotations();
        int insertParamsStartIndex = ctMethodToPatch.getParameterTypes().length;
        if (!Modifier.isStatic(ctMethodToPatch.getModifiers())) {
            insertParamsStartIndex += 1;
        }
        String[] localVarTypeNames = new String[insertParamAnnotations.length - insertParamsStartIndex];
        for (int i = insertParamsStartIndex; i < insertParamAnnotations.length; ++i) {
            if (paramByRef(insertParamAnnotations[i])) {
                if (!insertParamTypes[i].isArray()) {
                    System.out.println("      WARNING: ByRef parameter is not array type");
                } else {
                    localVarTypeNames[i - insertParamsStartIndex] = insertParamTypes[i].getName();
                }
            }
        }

        String src = "{\n";
        // Setup array holders for each local variable
        for (int i = 0; i < info.localvars().length; ++i) {
            if (localVarTypeNames[i] != null) {
                src += localVarTypeNames[i] + " __" + info.localvars()[i] + " = new " + localVarTypeNames[i] + "{" + info.localvars()[i] + "};\n";
            }
        }

        src += patchMethod.getDeclaringClass().getName() + "." + patchMethod.getName() + "(";
        if (!Modifier.isStatic(ctMethodToPatch.getModifiers())) {
            if (src.charAt(src.length()-1) != '(') {
                src += ", ";
            }
            src += "$0";
        }
        if (src.charAt(src.length()-1) != '(') {
            src += ", ";
        }
        src += "$$";
        for (int i = 0; i < info.localvars().length; ++i) {
            src += ", ";
            if (localVarTypeNames[i] != null) {
                src += "__";
            }
            src += info.localvars()[i];
        }
        src += ");\n";

        String src2 = src;
        // Set local variables to changed values
        for (int i = 0; i < info.localvars().length; ++i) {
            if (localVarTypeNames[i] != null) {
                src += info.localvars()[i] + " = ";
                src2 += info.localvars()[i] + " = ";

                String typename = paramByRefTypename(insertParamAnnotations[i + insertParamsStartIndex]);
                if (!typename.isEmpty()) {
                    src += "(" + typename + ")";
                    src2 += "(com.megacrit.cardcrawl." + typename + ")";
                }
                src += "__" + info.localvars()[i] + "[0];\n";
                src2 += "__" + info.localvars()[i] + "[0];\n";
            }
        }
        src += "}";
        src2 += "}";
        if (Loader.DEBUG) {
            System.out.println(src);
        }
        try {
            ctMethodToPatch.insertAt(loc, src);
        } catch (CannotCompileException e) {
            try {
                ctMethodToPatch.insertAt(loc, src2);
            } catch (CannotCompileException e2) {
                throw e;
            }
        }
    }
}
