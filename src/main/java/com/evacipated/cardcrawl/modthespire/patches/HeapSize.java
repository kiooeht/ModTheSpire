package com.evacipated.cardcrawl.modthespire.patches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;

@SpirePatch2(
    clz = CardCrawlGame.class,
    method = "render"
)
public class HeapSize
{
    @SpireInsertPatch(
        loc = 450
    )
    public static void Insert(SpriteBatch ___sb)
    {
        // for testing heap usage, change to true
        if (false) {
            Runtime runtime = Runtime.getRuntime();
            long heapSize = runtime.totalMemory() / 1024;
            long heapMaxSize = runtime.maxMemory();
            long heapFreeSize = runtime.freeMemory();

            FontHelper.renderFontLeftTopAligned(
                ___sb,
                FontHelper.tipBodyFont,
                "Heap size: " + formatSize(heapSize) + "\n" +
                    "Max Heap size: " + formatSize(heapMaxSize) + "\n" +
                    "Free size: " + formatSize(heapFreeSize),
                10,
                Settings.HEIGHT - 130,
                Color.WHITE
            );
        }
    }

    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
