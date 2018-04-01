package com.evacipated.cardcrawl.modthespire.lib;

import javassist.CtBehavior;

public abstract class SpireInsertLocator
{
    public abstract int[] Locate(CtBehavior ctMethodToPatch) throws Exception;
}
