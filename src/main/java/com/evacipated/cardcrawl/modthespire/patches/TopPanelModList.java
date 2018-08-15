package com.evacipated.cardcrawl.modthespire.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.ui.panels.TopPanel;

@SpirePatch(
    cls="com.megacrit.cardcrawl.ui.panels.TopPanel",
    method="updateTips"
)
public class TopPanelModList
{
    private static final float MOD_LIST_TIP_X = 1550.0F * Settings.scale;
    private static final float MOD_LIST_TIP_Y = Settings.HEIGHT - 120.0F * Settings.scale;
    private static Hitbox hb;

    public static void Postfix(TopPanel __instance)
    {
        if (hb == null) {
            hb = new Hitbox(
                Settings.WIDTH - 225.0F * Settings.scale,
                Settings.HEIGHT - 120.0F * Settings.scale,
                300.0F * Settings.scale,
                40.0F * Settings.scale
                );
        }

        if (!Settings.hideTopBar) {
            hb.update();
            if (hb.hovered) {
                String header = "Mod";
                if (Loader.MODINFOS.length > 1) {
                    header += "s";
                }
                header += ":";
                String mods = "";
                for (int i = 0; i < Loader.MODINFOS.length; ++i) {
                    if (i > 0) {
                        mods += " NL ";
                    }
                    mods += Loader.MODINFOS[i].Name;
                }
                TipHelper.renderGenericTip(MOD_LIST_TIP_X, MOD_LIST_TIP_Y, header, mods);
            }
        }
    }

    @SpirePatch(
        cls="com.megacrit.cardcrawl.ui.panels.TopPanel",
        method="render"
    )
    public static class Render
    {
        public static void Postfix(TopPanel __instance, SpriteBatch sb)
        {
            if (hb != null) {
                hb.render(sb);
            }
        }
    }
}
