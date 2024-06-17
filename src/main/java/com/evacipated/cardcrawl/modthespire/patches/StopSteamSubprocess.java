package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.steam.SteamWorkshopRunner;
import com.megacrit.cardcrawl.integrations.steam.SteamIntegration;

@SpirePatch2(
    clz = SteamIntegration.class,
    method = SpirePatch.CONSTRUCTOR
)
public class StopSteamSubprocess
{
    public static void Postfix()
    {
        SteamWorkshopRunner.stopAPI();
    }
}
