package com.evacipated.cardcrawl.modthespire.patcher;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import javassist.CtBehavior;

public class LocatorInfo {
	
	private CtBehavior ctMethodToPatch;
	private Class<?> finderClass;
	
	public LocatorInfo(CtBehavior ctMethodToPatch, Class<?> finderClass) {
		this.ctMethodToPatch = ctMethodToPatch;
		this.finderClass = finderClass;
	}
	
	public int[] findLines() throws Exception {
        SpireInsertLocator obj = (SpireInsertLocator)finderClass.newInstance();
        return obj.Locate(ctMethodToPatch);
	}

}
