package com.evacipated.cardcrawl.modthespire;

import java.net.URL;

class ModUpdate
{
    ModInfo info;
    URL releaseURL;
    URL downloadURL;

    ModUpdate(ModInfo info, URL releaseURL, URL downloadURL)
    {
        this.info = info;
        this.releaseURL = releaseURL;
        this.downloadURL = downloadURL;
    }
}
