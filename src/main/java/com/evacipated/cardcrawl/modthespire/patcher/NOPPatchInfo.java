package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.lib.SpireNOP;
import com.evacipated.cardcrawl.modthespire.patcher.javassist.MyCodeConverter;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtMethod;

public class NOPPatchInfo extends PatchInfo
{
    private SpireNOP spireNOP;

    public NOPPatchInfo(CtBehavior ctMethodToPatch, CtMethod patchMethod, SpireNOP spireNOP)
    {
        super(ctMethodToPatch, patchMethod);
        this.spireNOP = spireNOP;
    }

    @Override
    protected String debugMsg()
    {
        int startLoc = ctMethodToPatch.getMethodInfo().getLineNumber(0) + spireNOP.start_rloc();
        int endLoc = ctMethodToPatch.getMethodInfo().getLineNumber(0) + spireNOP.end_rloc();

        StringBuilder msgBuilder = new StringBuilder("NOPing from r");
        msgBuilder.append(spireNOP.start_rloc());
        msgBuilder.append(" (abs:");
        msgBuilder.append(startLoc);
        msgBuilder.append(") to r");
        msgBuilder.append(spireNOP.end_rloc());
        msgBuilder.append(" (abs:");
        msgBuilder.append(endLoc);
        msgBuilder.append(")...");

        return msgBuilder.toString();
    }

    @Override
    public int patchOrdering()
    {
        return 7;
    }

    @Override
    public void doPatch() throws PatchingException
    {
        int startLoc = ctMethodToPatch.getMethodInfo().getLineNumber(0) + spireNOP.start_rloc();
        int endLoc = ctMethodToPatch.getMethodInfo().getLineNumber(0) + spireNOP.end_rloc();

        MyCodeConverter codeConverter = new MyCodeConverter();
        codeConverter.insertGoto(startLoc, endLoc);
        try {
            ctMethodToPatch.instrument(codeConverter);
        } catch (CannotCompileException e) {
            throw new PatchingException(e);
        }
    }
}
