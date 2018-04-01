package com.evacipated.cardcrawl.modthespire.patcher;

import javassist.CtBehavior;

public class PatchingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 201498976754849825L;

	public PatchingException(CtBehavior m, String msg) {
		super(m.getDeclaringClass().getName() + "." + m.getName() + ": " + msg);
	}
	
}
