package com.evacipated.cardcrawl.modthespire.patches.modsscreen.BaseMod;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import javassist.CannotCompileException;
import javassist.CtBehavior;

@SpirePatch(
    cls="basemod.ModBadge",
    method="onClick",
    optional=true
)
public class ModBadgeOnClick
{
    public static void Raw(CtBehavior ctMethodToPatch)
    {
        try {
            ctMethodToPatch.setBody(
                "if (modPanel != null) {" +
                    "modPanel.oldInputProcessor = com.badlogic.gdx.Gdx.input.getInputProcessor();" +
                    "basemod.BaseMod.modSettingsUp = true;" +
                    "modPanel.isUp = true;" +
                    "modPanel.onCreate();" +
                "}"
            );
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }
}
