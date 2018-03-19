package com.evacipated.cardcrawl.modthespire;

public class DuplicateModIDException extends Exception
{
    private ModInfo info1;
    private ModInfo info2;

    public DuplicateModIDException(ModInfo info1, ModInfo info2)
    {
        this.info1 = info1;
        this.info2 = info2;
    }

    @Override
    public String getMessage()
    {
        return "Duplicate Mod ID: '" + info1.ID + "'. Used by '" + info1.Name + "' and '" + info2.Name + "'.";
    }
}
