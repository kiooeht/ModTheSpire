package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.Loader;

import javassist.*;

public class PostfixPatchInfo extends PatchInfo
{
    public PostfixPatchInfo(CtBehavior ctMethodToPatch, CtMethod patchMethod)
    {
        super(ctMethodToPatch, patchMethod);
    }

    @Override
    protected String debugMsg()
    {
        return "Adding Postfix...";
    }

    @Override
    public int patchOrdering()
    {
        return 2;
    }

    @Override
    public void doPatch() throws PatchingException
    {
        try {
            CtClass returnType = patchMethod.getReturnType();
            CtClass[] parameters = patchMethod.getParameterTypes();

            boolean returnsValue = false;
            boolean takesResultParam = false;

            if (!returnType.equals(CtPrimitiveType.voidType)) {
                returnsValue = true;
                if (Loader.DEBUG) {
                    System.out.println("      Return: " + returnType.getName());
                }
            }
            if (parameters.length >= 1 && parameters[0].equals(returnType)) {
                takesResultParam = true;
                if (Loader.DEBUG) {
                    System.out.println("      Result param: " + parameters[0].getName());
                }
            }

            String src = patchMethod.getDeclaringClass().getName() + "." + patchMethod.getName() + "(";
            if (returnsValue) {
                src = "return ($r)" + src;
            }
            if (takesResultParam) {
                src += "$_";
            }
            if (!Modifier.isStatic(ctMethodToPatch.getModifiers())) {
                if (src.charAt(src.length() - 1) != '(') {
                    src += ", ";
                }
                src += "$0";
            }
            if (src.charAt(src.length() - 1) != '(') {
                src += ", ";
            }
            src += "$$);";
            if (Loader.DEBUG) {
                System.out.println("      " + src);
            }
            ctMethodToPatch.insertAfter(src);
        } catch (CannotCompileException | NotFoundException e) {
            throw new PatchingException(e);
        }
    }
}
