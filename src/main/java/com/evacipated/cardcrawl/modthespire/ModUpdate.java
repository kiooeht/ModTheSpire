package com.evacipated.cardcrawl.modthespire;

import java.net.URL;

public class ModUpdate
{
    public ModInfo info;
    public URL releaseURL;
    public URL downloadURL;

    public ModUpdate(ModInfo info, URL releaseURL, URL downloadURL)
    {
        this.info = info;
        this.releaseURL = releaseURL;
        this.downloadURL = downloadURL;
    }
}
