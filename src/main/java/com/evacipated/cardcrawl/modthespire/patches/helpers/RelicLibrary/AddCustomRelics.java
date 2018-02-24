package com.evacipated.cardcrawl.modthespire.patches.helpers.RelicLibrary;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;

@SpirePatch(
    cls="com.megacrit.cardcrawl.helpers.RelicLibrary",
    method="initialize"
)
public class AddCustomRelics
{
    public static void Prefix()
    {
        ClassLoader loader = RelicLibrary.class.getClassLoader();

        for (String relicClassName : Loader.MODRELICS) {
            try {
                AbstractRelic relic = (AbstractRelic) loader.loadClass(relicClassName).newInstance();
                RelicLibrary.add(relic);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
