package com.evacipated.cardcrawl.modthespire.finders;

import com.evacipated.cardcrawl.modthespire.patcher.Expectation;

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

public abstract class MatchFinderExprEditor extends ExprEditor {

    protected abstract void doMatch(Expectation expectedType, Expr toMatch);

    public abstract boolean didFindLocation();

    public abstract int[] getFoundLocations();

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

}
