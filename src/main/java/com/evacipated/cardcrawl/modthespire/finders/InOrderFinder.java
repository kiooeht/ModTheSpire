package com.evacipated.cardcrawl.modthespire.finders;

import java.util.List;

import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.patcher.Expectation;

import javassist.expr.Expr;

public class InOrderFinder extends MatchFinderExprEditor {

    private int location;
    private boolean foundLocation;
    private int foundMatchesIndex;

    private Matcher finalMatch;
    private List<Matcher> expectedMatches;

    public InOrderFinder(List<Matcher> expectedMatches, Matcher finalMatch) {
        super();

        this.expectedMatches = expectedMatches;
        this.finalMatch = finalMatch;

        this.foundMatchesIndex = 0;
        this.foundLocation = false;
    }

    private void foundFinalMatch(int lineNumber) {
        if (foundLocation) return;

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

    @Override
    protected void doMatch(Expectation expectedType, Expr toMatch) {
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
    public boolean didFindLocation() {
        return foundLocation;
    }

    @Override
    public int[] getFoundLocations() {
        return new int[] {location};
    }
}
