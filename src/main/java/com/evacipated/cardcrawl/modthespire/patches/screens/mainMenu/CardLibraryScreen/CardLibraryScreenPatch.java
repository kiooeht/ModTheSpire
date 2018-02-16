package com.evacipated.cardcrawl.modthespire.patches.screens.mainMenu.CardLibraryScreen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.screens.mainMenu.CardLibSortHeader;
import com.megacrit.cardcrawl.screens.mainMenu.CardLibraryScreen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CardLibraryScreenPatch
{
    @SpirePatch(
        cls="com.megacrit.cardcrawl.screens.mainMenu.CardLibraryScreen",
        method="initialize"
    )
    public static class Initialize
    {
        public static Map<AbstractCard.CardColor, CardGroup> cardGroupMap = new HashMap<>();
        public static Map<AbstractCard.CardColor, CardLibSortHeader> cardHeaderMap = new HashMap<>();

        @SpireInsertPatch(rloc=37)
        public static void Insert(Object __obj_instance)
        {
            try {
                CardLibraryScreen screen = (CardLibraryScreen) __obj_instance;

                AbstractCard.CardColor[] colors = AbstractCard.CardColor.values();
                for (int icolor = AbstractCard.CardColor.CURSE.ordinal() + 1; icolor < colors.length; ++icolor) {
                    CardGroup group = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
                    group.group = CardLibrary.getCardList(CardLibrary.LibraryType.valueOf(colors[icolor].name()));
                    cardGroupMap.put(colors[icolor], group);

                    CardLibSortHeader header = new CardLibSortHeader(group);
                    cardHeaderMap.put(colors[icolor], header);
                    Field headersField = CardLibraryScreen.class.getDeclaredField("headers");
                    headersField.setAccessible(true);
                    ArrayList<CardLibSortHeader> headers = (ArrayList<CardLibSortHeader>)headersField.get(screen);
                    headers.add(header);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SpirePatch(
        cls="com.megacrit.cardcrawl.screens.mainMenu.CardLibraryScreen",
        method="calculateScrollBounds"
    )
    public static class CalculateScrollBounds
    {
        @SpireInsertPatch(
            rloc=3,
            localvars={"size"}
        )
        public static void Insert(Object __obj_instance, @ByRef int[] size)
        {
            System.out.println(size[0]);
            for (Map.Entry<AbstractCard.CardColor, CardGroup> cards : Initialize.cardGroupMap.entrySet()) {
                size[0] += cards.getValue().size();
            }
            System.out.println(size[0]);
        }
    }

    @SpirePatch(
        cls = "com.megacrit.cardcrawl.screens.mainMenu.CardLibraryScreen",
        method = "updateCards"
    )
    public static class UpdateCards
    {
        @SpireInsertPatch(
            rloc = 81,
            localvars = {"lineNum", "drawStartX", "drawStartY", "padX", "padY", "currentDiffY"}
        )
        public static void Insert(Object __obj_instance, @ByRef int[] lineNum, float drawStartX, float drawStartY, float padX, float padY, float currentDiffY)
        {
            try {
                Field hoveredCard = CardLibraryScreen.class.getDeclaredField("hoveredCard");
                hoveredCard.setAccessible(true);

                lineNum[0] += 2;
                ArrayList<AbstractCard> cards;
                CardLibSortHeader header;
                for (Map.Entry<AbstractCard.CardColor, CardGroup> entry : Initialize.cardGroupMap.entrySet()) {
                    cards = entry.getValue().group;
                    header = Initialize.cardHeaderMap.get(entry.getKey());

                    for (int i = 0; i < cards.size(); ++i) {
                        int mod = i % 5;
                        if ((mod == 0) && i != 0) {
                            lineNum[0]++;
                        }
                        cards.get(i).target_x = drawStartX + mod * padX;
                        cards.get(i).target_y = drawStartY + currentDiffY - lineNum[0] * padY;
                        cards.get(i).update();
                        cards.get(i).updateHoverLogic();
                        if (cards.get(i).hb.hovered) {
                            hoveredCard.set(__obj_instance, cards.get(i));
                        }
                    }

                    if (header.justSorted) {
                        for (AbstractCard c : cards) {
                            c.current_x = c.target_x;
                            c.current_y = c.target_y;
                        }
                        header.justSorted = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SpirePatch(
        cls="com.megacrit.cardcrawl.screens.mainMenu.CardLibraryScreen",
        method="render"
    )
    public static class Render
    {
        @SpireInsertPatch(
            rloc=11
        )
        public static void Insert(Object __obj_instance, Object sbObj)
        {
            try {
                Method renderGroup = CardLibraryScreen.class.getDeclaredMethod("renderGroup", SpriteBatch.class, CardGroup.class, String.class, String.class);
                renderGroup.setAccessible(true);

                for (Map.Entry<AbstractCard.CardColor, CardGroup> cards : Initialize.cardGroupMap.entrySet()) {
                    renderGroup.invoke(__obj_instance, sbObj, cards.getValue(), cards.getKey().name(), "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
