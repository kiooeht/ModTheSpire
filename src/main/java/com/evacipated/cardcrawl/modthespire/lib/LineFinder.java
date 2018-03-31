package com.evacipated.cardcrawl.modthespire.lib;

import java.util.List;

import com.evacipated.cardcrawl.modthespire.finders.InOrderFinder;
import com.evacipated.cardcrawl.modthespire.finders.InOrderMultiFinder;
import com.evacipated.cardcrawl.modthespire.finders.MatchFinderExprEditor;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;

import javassist.CannotCompileException;
import javassist.CtBehavior;

public class LineFinder {

	private LineFinder() {}
	
	public static final int[] findAllInOrder(CtBehavior ctMethodToPatch, List<Matcher> expectedMatches, Matcher finalMatch) throws CannotCompileException, PatchingException {
		MatchFinderExprEditor editor = new InOrderMultiFinder(expectedMatches, finalMatch);
		ctMethodToPatch.instrument(editor);
		if (!editor.didFindLocation()) {
			throw new PatchingException("    ERROR: Location matching given description could not be found for patch on " + ctMethodToPatch.getName() + "!");
		}
		return editor.getFoundLocations();
	}
	
	public static final int[] findInOrder(CtBehavior ctMethodToPatch, List<Matcher> expectedMatches, Matcher finalMatch) throws CannotCompileException, PatchingException {
		MatchFinderExprEditor editor = new InOrderFinder(expectedMatches, finalMatch);
		ctMethodToPatch.instrument(editor);
		if (!editor.didFindLocation()) {
			throw new PatchingException("    ERROR: Location matching given description could not be found for patch on " + ctMethodToPatch.getName() + "!");
		}
		return editor.getFoundLocations();
	}
	
}
