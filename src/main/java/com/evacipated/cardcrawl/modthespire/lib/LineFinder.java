package com.evacipated.cardcrawl.modthespire.lib;

import java.util.List;

import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.Cast;
import javassist.expr.ConstructorCall;
import javassist.expr.Expr;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.Handler;
import javassist.expr.Instanceof;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;
import javassist.expr.NewExpr;

public class LineFinder {

	private LineFinder() {}
	
	public static class MatchFinderExprEditor extends ExprEditor {
		private int location;
		private boolean foundLocation;
		private int foundMatchesIndex;
		
		private Matcher finalMatch;
		private List<Matcher> expectedMatches;
		
		public MatchFinderExprEditor(List<Matcher> expectedMatches, Matcher finalMatch) {
			this.expectedMatches = expectedMatches;
			this.finalMatch = finalMatch;
			
			this.foundMatchesIndex = 0;
			this.foundLocation = false;
		}
		
		private void foundFinalMatch(int lineNumber) {
			this.foundLocation = true;
			this.location = lineNumber;
		}
		
		private boolean finalMatch() {
			return foundMatchesIndex >= expectedMatches.size();
		}
		
		private void foundMatch() {
			this.foundMatchesIndex++;
		}
		
		private Matcher currentMatch() {
			return expectedMatches.get(foundMatchesIndex);
		}
		
		private void doMatch(Expectation expectedType, Expr toMatch) {
			if (finalMatch()) {
				if (finalMatch.getExpectation() == expectedType && finalMatch.match(toMatch)) {
					foundFinalMatch(toMatch.getLineNumber());
				}
			} else {
				Matcher current = currentMatch();
				if (current.getExpectation() == expectedType && current.match(toMatch)) {
					foundMatch();
				}
			}
		}
		
		@Override
		public void edit(Cast expr) {
			doMatch(Expectation.TYPE_CAST, expr);
		}
		
		@Override
		public void edit(ConstructorCall expr) {
			doMatch(Expectation.CONSTRUCTOR_CALL, expr);
		}
		
		@Override
		public void edit(FieldAccess expr) {
			doMatch(Expectation.FIELD_ACCESS, expr);
		}
		
		@Override
		public void edit(Handler expr) {
			doMatch(Expectation.CATCH_CLAUSE, expr);
		}
		
		@Override
		public void edit(Instanceof expr) {
			doMatch(Expectation.INSTANCEOF, expr);
		}
		
		@Override
		public void edit(MethodCall expr) {
			doMatch(Expectation.METHOD_CALL, expr);
		}
		
		@Override
		public void edit(NewArray expr) {
			doMatch(Expectation.ARRAY_CREATION, expr);
		}
		
		@Override
		public void edit(NewExpr expr) {
			doMatch(Expectation.NEW_EXPRESSION, expr);
		}
		
		public boolean didFindLocation() {
			return foundLocation;
		}
		
		public int getFoundLocation() {
			return location;
		}
		
	}
	
	public static final int findInOrder(CtBehavior ctMethodToPatch, List<Matcher> expectedMatches, Matcher finalMatch) throws CannotCompileException, PatchingException {
		MatchFinderExprEditor editor = new MatchFinderExprEditor(expectedMatches, finalMatch);
		ctMethodToPatch.instrument(editor);
		if (!editor.didFindLocation()) {
			throw new PatchingException("    ERROR: Location matching given description could not be found!");
		}
		return editor.getFoundLocation();
	}
	
}
