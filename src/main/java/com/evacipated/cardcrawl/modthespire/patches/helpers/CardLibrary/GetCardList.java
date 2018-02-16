package com.evacipated.cardcrawl.modthespire.patches.helpers.CardLibrary;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.status.Burn;
import com.megacrit.cardcrawl.cards.status.Dazed;
import com.megacrit.cardcrawl.cards.status.Wound;
import com.megacrit.cardcrawl.helpers.CardLibrary;

import java.util.ArrayList;
import java.util.Map;

@SpirePatch(
    cls="com.megacrit.cardcrawl.helpers.CardLibrary",
    method="getCardList"
)
public class GetCardList
{
    public static Object Replace(Object objType)
    {
        CardLibrary.LibraryType type = (CardLibrary.LibraryType)objType;

        ArrayList<AbstractCard> ret = new ArrayList<>();

        if (type == CardLibrary.LibraryType.STATUS) {
            ret.add(new Wound());
            ret.add(new Burn());
            ret.add(new Dazed());
        } else {
            for (Map.Entry<String, AbstractCard> c : CardLibrary.cards.entrySet()) {
                if (c.getValue().color.name().equals(type.name())) {
                    ret.add(c.getValue());
                }
            }
        }

        return ret;
    }
}
