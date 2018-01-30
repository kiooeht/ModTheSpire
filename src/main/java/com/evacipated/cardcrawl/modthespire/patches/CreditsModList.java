package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.credits.CreditLine;
import com.megacrit.cardcrawl.credits.CreditsScreen;

import java.lang.reflect.Field;
import java.util.ArrayList;

@SpirePatch(
        cls="com.megacrit.cardcrawl.credits.CreditsScreen",
        method="ctor"
)
public class CreditsModList {
    private static String[] MTS_AUTHORS = new String[] {
            "kiooeht",
            "t-larson"
    };

    @SpireInsertPatch(
            loc=66,
            localvars={"tmpY"}
    )
    public static void Insert(Object __obj_instance, @ByRef float[] tmpY)
    {
        try {
            Field f = CreditsScreen.class.getDeclaredField("lines");
            f.setAccessible(true);
            ArrayList<CreditLine> lines = (ArrayList<CreditLine>)f.get(__obj_instance);

            // ModTheSpire
            lines.add(new CreditLine("ModTheSpire", tmpY[0] -= 150.0F, true));
            // ModTheSpire authors
            for (String author : MTS_AUTHORS) {
                lines.add(new CreditLine(author, tmpY[0] -= 45.0F, false));
            }
            // Loaded mods
            for (ModInfo info : Loader.MODINFOS) {
                lines.add(new CreditLine(info.Name, tmpY[0] -= 150.0F, true));
                if (!info.Author.isEmpty()) {
                    String[] modAuthors = info.Author.split(",");
                    for (String author : modAuthors) {
                        lines.add(new CreditLine(author, tmpY[0] -= 45.0F, false));
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
