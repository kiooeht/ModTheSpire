package com.evacipated.cardcrawl.modthespire.patches.helpers.CardLibrary;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.CardLibrary;

@SpirePatch(
    cls="com.megacrit.cardcrawl.helpers.CardLibrary",
    method="initialize"
)
public class AddCustomCards
{
    @SpireInsertPatch(
        rloc=9
    )
    public static void Insert()
    {
        ClassLoader loader = CardLibrary.class.getClassLoader();

        for (String cardClassName : Loader.MODCARDS) {
            try {
                AbstractCard card = (AbstractCard) loader.loadClass(cardClassName).newInstance();
                CardLibrary.add(card);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
