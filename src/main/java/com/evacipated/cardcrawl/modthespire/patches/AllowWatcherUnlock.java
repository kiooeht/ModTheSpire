package com.evacipated.cardcrawl.modthespire.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.stats.CharStat;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import com.megacrit.cardcrawl.unlock.misc.DefectUnlock;
import com.megacrit.cardcrawl.unlock.misc.WatcherUnlock;

import java.util.ArrayList;
import java.util.List;

@SpirePatch2(
    clz=DeathScreen.class,
    method="willWatcherUnlock"
)
public class AllowWatcherUnlock
{
    public static SpireReturn<Boolean> Prefix(boolean ___defectUnlockedThisRun)
    {
        if (___defectUnlockedThisRun || !UnlockTracker.isCharacterLocked(WatcherUnlock.KEY)) {
            return SpireReturn.Return(false);
        }
        List<CharStat> baseCharacters = new ArrayList<>();
        for (AbstractPlayer c : CardCrawlGame.characterManager.getAllCharacters()) {
            switch (c.chosenClass) {
                case IRONCLAD:
                case THE_SILENT:
                case DEFECT:
                    baseCharacters.add(c.getCharStat());
                    break;
            }
        }
        return SpireReturn.Return(
            !UnlockTracker.isCharacterLocked(DefectUnlock.KEY)
            && baseCharacters.stream().anyMatch(stats -> stats.getVictoryCount() > 0)
        );
    }
}
