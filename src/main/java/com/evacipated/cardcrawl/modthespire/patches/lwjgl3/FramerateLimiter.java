package com.evacipated.cardcrawl.modthespire.patches.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import javassist.CtBehavior;

@SpirePatch2(
    clz = Lwjgl3Application.class,
    method = "loop"
)
public class FramerateLimiter
{
    public static int fps = 60;
    private static final Sync sync = new Sync();

    @SpireInsertPatch(
        loc = 180
    )
    public static void Insert()
    {
        sync.sync(fps);
    }

    private static class Locator extends SpireInsertLocator
    {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception
        {
            Matcher matcher = new Matcher.FieldAccessMatcher(AbstractPlayer.class, "powers");
            return LineFinder.findInOrder(ctBehavior, matcher);
        }
    }
}
