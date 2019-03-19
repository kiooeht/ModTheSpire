package com.evacipated.cardcrawl.modthespire.patches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

@SpirePatch(
    clz=TopPanel.class,
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
        clz=TopPanel.class,
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

        public static ExprEditor Instrument()
        {
            return new ExprEditor() {
                private int count = 0;

                @Override
                public void edit(MethodCall m) throws CannotCompileException
                {
                    if (m.getMethodName().equals("renderFontRightTopAligned")) {
                        ++count;
                        if (count == 2) {
                            m.replace(
                                "{" +
                                    String.format("$5 -= 24 * %s.scale;", Settings.class.getName()) +
                                    "$_ = $proceed($$);" +
                                    "}"
                            );
                        }
                    }
                }

                @Override
                public void edit(FieldAccess f) throws CannotCompileException
                {
                    if (f.getFieldName().equals("VERSION_NUM")) {
                        f.replace(String.format("$_ = %s.alterVersion2($proceed($$));", MainMenuModList.class.getName()));
                    }
                }
            };
        }

        @SpireInsertPatch(
            locator=Locator.class
        )
        public static void Insert(TopPanel __instance, SpriteBatch sb)
        {
            FontHelper.renderFontRightTopAligned(
                sb,
                FontHelper.cardDescFont_N,
                MainMenuModList.makeMTSVersionModCount("ModTheSpire " + Loader.MTS_VERSION),
                Settings.WIDTH - 16 * Settings.scale,
                Settings.HEIGHT - 104 * Settings.scale,
                new Color(1, 1, 1, 0.3f)
            );
        }

        private static class Locator extends SpireInsertLocator
        {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
            {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(SeedHelper.class, "getUserFacingSeedString");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }
}
