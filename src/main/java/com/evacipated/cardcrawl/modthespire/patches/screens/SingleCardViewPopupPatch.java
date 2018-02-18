package com.evacipated.cardcrawl.modthespire.patches.screens;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.patches.screens.mainMenu.CardLibraryScreen.CardLibraryScreenPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;

public class SingleCardViewPopupPatch
{
    @SpirePatch(
        cls="com.megacrit.cardcrawl.screens.SingleCardViewPopup",
        method="open"
    )
    public static class Open
    {
        public static void Prefix(Object __obj_instance, Object cardObj, @ByRef Object[] groupObj)
        {
            if (groupObj[0] == null) {
                AbstractCard card = (AbstractCard)cardObj;
                groupObj[0] = CardLibraryScreenPatch.Initialize.cardGroupMap.get(card.color);
            }
        }
    }
}
