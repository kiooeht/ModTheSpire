package com.evacipated.cardcrawl.modthespire.lib;

import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class LineFinder {

	private LineFinder() {}
	
	public static class MethodFinderExprEditor extends ExprEditor {
		private int location;
		private boolean foundLocation;
		private int foundMethodIndex;
		
		private String className, methodName;
		private String[] previousCalls;
		
		public MethodFinderExprEditor(String className, String methodName, String[] previousCalls) {
			this.className = className;
			this.methodName = methodName;
			this.previousCalls = previousCalls;
			
			this.foundMethodIndex = 0;
			this.foundLocation = false;
		}
		
		@Override
		public void edit(MethodCall m) {
			if (foundLocation) return;
			
			if (foundMethodIndex >= previousCalls.length / 2) {
				if (m.getClassName().equals(className) &&
						m.getMethodName().equals(methodName)) {
					this.foundLocation = true;
					this.location = m.getLineNumber();
				}
			} else {
				String objectName = previousCalls[foundMethodIndex * 2];
				String methodName = previousCalls[foundMethodIndex * 2 + 1];
				if (m.getClassName().equals(objectName) && m.getMethodName().equals(methodName)) {
					foundMethodIndex++;
				}
			}
		}
		
		public boolean didFindLocation() {
			return foundLocation;
		}
		
		public int getFoundLocation() {
			return location;
		}
		
	}
	
	public static final int findByMethods(CtBehavior ctMethodToPatch, String className, String methodName,
			String[] previousCalls) throws CannotCompileException, PatchingException {
		MethodFinderExprEditor editor = new MethodFinderExprEditor(className, methodName, previousCalls);
		ctMethodToPatch.instrument(editor);
		if (!editor.didFindLocation()) {
			throw new PatchingException("    ERROR: Location matching given description could not be found!");
		}
		return editor.getFoundLocation();
	}
	
}
