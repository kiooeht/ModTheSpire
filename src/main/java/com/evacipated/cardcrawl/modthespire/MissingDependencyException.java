package com.evacipated.cardcrawl.modthespire;

public class MissingDependencyException extends Exception
{
    public ModInfo modInfo;
    public String dependency;

    public MissingDependencyException(ModInfo modInfo, String dependency)
    {
        this.modInfo = modInfo;
        this.dependency = dependency;
    }

    @Override
    public String getMessage()
    {
        String ret = "Unable to find dependency '" + dependency + "' for mod '";
        if (modInfo.ID == null || modInfo.ID.isEmpty()) {
            ret += modInfo.Name;
        } else {
            ret += modInfo.ID;
        }
        ret += "'.";
        return ret;
    }
}
