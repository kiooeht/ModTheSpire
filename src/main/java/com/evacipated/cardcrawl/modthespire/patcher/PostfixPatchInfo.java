package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.Loader;

import javassist.*;

public class PostfixPatchInfo extends ParameterPatchInfo
{
    private boolean returnsValue = false;
    private boolean takesResultParam = false;

    public PostfixPatchInfo(CtBehavior ctMethodToPatch, CtMethod patchMethod)
    {
        super(ctMethodToPatch, patchMethod);
        canByRefParams = true;
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
    protected ParamTransformer makeTransformer(ParamInfo src, ParamInfo dest)
    {
        return new PostfixParamTransformer(src, dest);
    }

    @Override
    protected ParamInfo2 makeInfo2(CtBehavior toPatch, CtMethod patchMethod, int position) throws PatchingException
    {
        return new PostfixParamInfo2(toPatch, patchMethod, position);
    }

    @Override
    protected void alterSrc()
    {
        if (returnsValue) {
            funccall = "return ($r)" + funccall;
        }
    }

    @Override
    protected void applyPatch(String src) throws CannotCompileException
    {
        ctMethodToPatch.insertAfter(src);
    }

    protected class PostfixParamTransformer extends ParamTransformer
    {
        protected PostfixParamTransformer(ParamInfo src, ParamInfo dest)
        {
            super(src, dest);
        }

        @Override
        protected boolean advanceSrcPosition()
        {
            if (destInfo.getPosition() == 1 && takesResultParam) {
                return false;
            }
            return super.advanceSrcPosition();
        }

        @Override
        protected String getParamName() throws PatchingException
        {
            if (destInfo.getPosition() == 1) {
                try {
                    CtClass returnType = patchMethod.getReturnType();

                    if (!returnType.equals(CtPrimitiveType.voidType)) {
                        returnsValue = true;
                        if (Loader.DEBUG) {
                            System.out.println("      Return: " + returnType.getName());
                        }
                    }
                    if (destInfo.getType().equals(returnType)) {
                        takesResultParam = true;
                        if (Loader.DEBUG) {
                            System.out.println("      Result param: " + destInfo.getTypename());
                        }
                        return "$_";
                    }
                } catch (NotFoundException e) {
                    throw new PatchingException(e);
                }
            }
            return super.getParamName();
        }
    }

    protected class PostfixParamInfo2 extends ParamInfo2
    {
        PostfixParamInfo2(CtBehavior toPatch, CtMethod patchMethod, int position) throws PatchingException
        {
            super(toPatch, patchMethod, position);
        }

        @Override
        protected boolean specialNameCheck(String patchParamName) throws PatchingException
        {
            if (patchParamName.equals("__result")) {
                try {
                    CtClass returnType = patchMethod.getReturnType();
                    if (!returnType.equals(CtPrimitiveType.voidType)) {
                        returnsValue = true;
                        if (Loader.DEBUG) {
                            System.out.println("      Return: " + returnType.getName());
                        }
                    }

                    takesResultParam = true;
                    if (Loader.DEBUG) {
                        System.out.println("      Result param: " + getPatchParamTypename());
                    }
                } catch (NotFoundException e) {
                    throw new PatchingException(e);
                }
                name = patchParamName;
                argName = "$_";
                return true;
            }
            return false;
        }
    }
}
