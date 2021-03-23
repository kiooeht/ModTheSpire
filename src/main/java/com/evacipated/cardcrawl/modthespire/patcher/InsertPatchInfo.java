package com.evacipated.cardcrawl.modthespire.patcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import javassist.*;

public class InsertPatchInfo extends ParameterPatchInfo
{

    public static class LineNumberAndPatchType {
        public int lineNumber;
        public int relativeLineNumber;
        public InsertPatchType patchType;

        public LineNumberAndPatchType(int lineNumber) {
            this.lineNumber = lineNumber;
            this.patchType = InsertPatchType.ABSOLUTE;
        }

        public LineNumberAndPatchType(int lineNumber, int relativeLineNumber) {
            this.lineNumber = lineNumber;
            this.relativeLineNumber = relativeLineNumber;
            this.patchType = InsertPatchType.RELATIVE;
        }

    }

    public static enum InsertPatchType {
        ABSOLUTE, RELATIVE
    }

    private SpireInsertPatch info;
    private List<LineNumberAndPatchType> locs;

    public InsertPatchInfo(SpireInsertPatch info, List<LineNumberAndPatchType> locs, CtBehavior ctMethodToPatch, CtMethod patchMethod)
    {
        super(ctMethodToPatch, patchMethod);
        canSpireReturn = true;
        canByRefParams = true;
        this.info = info;
        this.locs = locs;
    }

    @Override
    protected String debugMsg()
    {
        StringBuilder msgBuilder = new StringBuilder("");
        for (LineNumberAndPatchType patchLoc : locs) {
            switch(patchLoc.patchType) {
            case ABSOLUTE:
                msgBuilder.append("Adding Insert @ " + patchLoc.lineNumber + "...\n");
                break;
            case RELATIVE:
                msgBuilder.append("Adding Insert @ r" + patchLoc.relativeLineNumber + " (abs:" + patchLoc.lineNumber + ")...\n");
                break;
            }
        }
        return msgBuilder.toString();
    }

    @Override
    public int patchOrdering()
    {
        return -2;
    }
    
    @Override
    protected ParamTransformer makeTransformer(ParamInfo src, ParamInfo dest)
    {
        return new InsertParamTransformer(src, dest);
    }

    @Override
    protected ParamInfo2 makeInfo2(CtBehavior toPatch, CtMethod patchMethod, int position) throws PatchingException
    {
        return new InsertParamInfo2(toPatch, patchMethod, position);
    }

    private void applyPatch(String src, int loc) throws CannotCompileException
    {
        ctMethodToPatch.insertAt(loc, src);
    }

    @Override
    protected void applyPatch(String src) throws CannotCompileException
    {
        for (LineNumberAndPatchType patchLoc : locs) {
            applyPatch(src, patchLoc.lineNumber);
        }
    }

    protected class InsertParamTransformer extends ParamTransformer
    {

        protected InsertParamTransformer(ParamInfo src, ParamInfo dest)
        {
            super(src, dest);
        }

        @Override
        protected String getParamName() throws PatchingException
        {
            int localVarStartPosition = destInfo.getParamCount() - info.localvars().length + 1;
            if (destInfo.getPosition() >= localVarStartPosition) {
                int idx = destInfo.getPosition() - localVarStartPosition;
                return info.localvars()[idx];
            }

            return super.getParamName();
        }
    }

    protected class InsertParamInfo2 extends ParamInfo2
    {
        InsertParamInfo2(CtBehavior toPatch, CtMethod patchMethod, int position) throws PatchingException
        {
            super(toPatch, patchMethod, position);
        }

        @Override
        protected boolean specialNameCheck(String patchParamName) throws PatchingException
        {
            if (Arrays.asList(info.localvars()).contains(patchParamName)) {
                name = patchParamName;
                argName = patchParamName;
                return true;
            }
            return false;
        }
    }
}
