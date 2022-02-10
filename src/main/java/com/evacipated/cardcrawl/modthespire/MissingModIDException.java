package com.evacipated.cardcrawl.modthespire;

public class MissingModIDException extends Exception
{
    public String modID;

    public MissingModIDException(String modID)
    {
        this.modID = modID;
    }

    @Override
    public String getMessage()
    {
        return "Unable to find Mod ID '" + modID + "'.";
    }
}
