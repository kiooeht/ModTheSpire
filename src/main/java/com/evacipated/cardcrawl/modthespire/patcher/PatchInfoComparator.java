package com.evacipated.cardcrawl.modthespire.patcher;

import java.util.Comparator;

public class PatchInfoComparator implements Comparator<PatchInfo>
{
    // Ordering:
    //   Insert, Instrument, Replace, Prefix, Postfix
    @Override
    public int compare(PatchInfo o1, PatchInfo o2)
    {
        int patchOrdering = Integer.compare(o1.patchOrdering(), o2.patchOrdering());
        if (patchOrdering != 0) {
            return patchOrdering;
        }

        return o1.toString().compareTo(o2.toString());
    }
}
