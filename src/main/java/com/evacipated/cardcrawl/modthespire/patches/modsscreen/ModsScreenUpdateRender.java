package com.evacipated.cardcrawl.modthespire.patches.modsscreen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;

public class ModsScreenUpdateRender
{
    @SpirePatch(
        cls="com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen",
        method="update"
    )
    public static class Update
    {
        public static void Postfix(MainMenuScreen __instance)
        {
            if (__instance.screen == ModsScreen.MODS_LIST) {
                ModMenuButton.modsScreen.update();
            }
        }
    }

    @SpirePatch(
        cls="com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen",
        method="render"
    )
    public static class Render
    {
        public static void Postfix(MainMenuScreen __instance, SpriteBatch sb)
        {
            if (__instance.screen == ModsScreen.MODS_LIST) {
                ModMenuButton.modsScreen.render(sb);
            }
        }
    }
}
