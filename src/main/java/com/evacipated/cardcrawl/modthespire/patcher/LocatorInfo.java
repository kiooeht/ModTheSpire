package com.evacipated.cardcrawl.modthespire.patcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.CtBehavior;

public class LocatorInfo {
	
	private CtBehavior ctMethodToPatch;
	private Method finderMethod;
	
	public LocatorInfo(CtBehavior ctMethodToPatch, Method finderMethod) {
		this.ctMethodToPatch = ctMethodToPatch;
		this.finderMethod = finderMethod;
	}
	
	public int[] findLines() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (int[]) finderMethod.invoke(null, ctMethodToPatch);
	}

}
