package com.evacipated.cardcrawl.modthespire.patcher;

import java.lang.annotation.Annotation;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireLocatorPatch;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class LocatorPatchInfo extends InsertPatchInfo {
	
	public LocatorPatchInfo(SpireLocatorPatch info, int loc, CtBehavior ctMethodToPatch, CtMethod patchMethod) {
		super(new SpireInsertPatch() {
			@Override
			public int loc() {return 0;}

			@Override
			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}

			@Override
			public int rloc() {return 0;}

			@Override
			public String[] localvars() {return new String[] {};}
		}, loc, ctMethodToPatch, patchMethod);
	}
	
	@Override
	protected String debugMsg() {
		return "Adding Locator @ " + loc + "...";
	}
	
	@Override
	public int patchOrdering() {
		return -3;
	}
	
	public static class MethodFinderExprEditor extends ExprEditor {
		private int location;
		private boolean foundLocation;
		private SpireLocatorPatch locatorPatch;
		private int foundMethodIndex;
		
		public MethodFinderExprEditor(SpireLocatorPatch locatorPatch) {
			this.locatorPatch = locatorPatch;
			this.foundMethodIndex = 0;
			this.foundLocation = false;
		}
		
		@Override
		public void edit(MethodCall m) {
			if (foundLocation) return;
			
			if (foundMethodIndex >= locatorPatch.previousCalls().length / 2) {
				if (m.getClassName().equals(locatorPatch.objectName()) &&
						m.getMethodName().equals(locatorPatch.methodName())) {
					this.foundLocation = true;
					this.location = m.getLineNumber();
				}
			} else {
				String objectName = locatorPatch.previousCalls()[foundMethodIndex * 2];
				String methodName = locatorPatch.previousCalls()[foundMethodIndex * 2 + 1];
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
	
	public static final int findLocation(CtBehavior ctMethodToPatch, SpireLocatorPatch locatorPatch) throws CannotCompileException, PatchingException {
		MethodFinderExprEditor editor = new MethodFinderExprEditor(locatorPatch);
		ctMethodToPatch.instrument(editor);
		if (!editor.didFindLocation()) {
			throw new PatchingException("    ERROR: Location matching given description could not be found!");
		}
		return editor.getFoundLocation();
	}

}
