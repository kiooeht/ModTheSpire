package com.evacipated.cardcrawl.modthespire.patches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.patches.modsscreen.ModsScreen;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;

@SpirePatch(
        cls="com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen",
        method="render"
)
public class MainMenuModList {
    public static void Postfix(Object __obj_instance, Object sb)
    {
        MainMenuScreen __instance = (MainMenuScreen)__obj_instance;

        if (__instance.screen != ModsScreen.MODS_LIST) {
            float tmpy = 30.0F;
            for (int i = Loader.MODINFOS.length - 1; i >= 0; --i) {
                FontHelper.renderFontRightTopAligned(
                    (SpriteBatch) sb,
                    FontHelper.cardDescFont_N,
                    Loader.MODINFOS[i].Name,
                    Settings.WIDTH - 16.0F * Settings.scale + 200.0F * __instance.bg.slider,
                    tmpy * Settings.scale,
                    new Color(1.0F, 1.0F, 1.0F, 0.3F)
                );
                tmpy += 30.0F;
            }

            // Mod(s): label
            Color gold = Settings.GOLD_COLOR.cpy();
            gold.a = 0.3F;
            FontHelper.renderFontRightTopAligned(
                (SpriteBatch) sb,
                FontHelper.cardDescFont_N,
                "Mod" + (Loader.MODINFOS.length > 1 ? "s" : "") + ":",
                Settings.WIDTH - 16.0F * Settings.scale + 200.0F * __instance.bg.slider,
                tmpy * Settings.scale,
                gold);
        }
    }
}
