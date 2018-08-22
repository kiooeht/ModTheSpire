package com.evacipated.cardcrawl.modthespire.lib;

import com.evacipated.cardcrawl.modthespire.patcher.Expectation;

import javassist.NotFoundException;
import javassist.expr.Cast;
import javassist.expr.ConstructorCall;
import javassist.expr.Expr;
import javassist.expr.FieldAccess;
import javassist.expr.Handler;
import javassist.expr.Instanceof;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;
import javassist.expr.NewExpr;

public abstract class Matcher {

    private Expectation expectedType;

    public Matcher(Expectation expectedType) {
        this.expectedType = expectedType;
    }

    public Expectation getExpectation() {
        return expectedType;
    }

    public abstract boolean match(Expr toMatch);

    public static class TypeCastMatcher extends Matcher {

        private String typeName;

        public TypeCastMatcher(String typeName) {
            super(Expectation.TYPE_CAST);

            this.typeName = typeName;
        }

        public boolean match(Expr toMatch) {
            Cast expr = (Cast) toMatch;

            boolean result = false;

            try {
                result = expr.getType().getName().equals(typeName);
            } catch (NotFoundException e) {
                // this is allowed to happen so eat it
            }

            return result;
        }

    }

    public static class ConstructorCallMatcher extends Matcher {

        private String className, methodName;
        private boolean checkMethodName;

        public ConstructorCallMatcher(Class<?> clazz) {
            this(clazz.getName());
        }

        public ConstructorCallMatcher(String className) {
            super(Expectation.CONSTRUCTOR_CALL);

            this.className = className;
            this.checkMethodName = false;
        }

        public ConstructorCallMatcher(Class<?> clazz, String methodName) {
            this(clazz.getName(), methodName);
        }

        public ConstructorCallMatcher(String className, String methodName) {
            super(Expectation.CONSTRUCTOR_CALL);

            this.className = className;
            this.methodName = methodName;
            this.checkMethodName = false;
        }

        public boolean match(Expr toMatch) {
            ConstructorCall expr = (ConstructorCall) toMatch;

            return expr.getClassName().equals(className) &&
                    (!checkMethodName || expr.getMethodName().equals(methodName));
        }

    }

    public static class FieldAccessMatcher extends Matcher {

        private String className, fieldName;

        public FieldAccessMatcher(Class<?> clazz, String fieldName) {
            this(clazz.getName(), fieldName);
        }

        public FieldAccessMatcher(String className, String fieldName) {
            super(Expectation.FIELD_ACCESS);

            this.className = className;
            this.fieldName = fieldName;
        }

        public boolean match(Expr toMatch) {
            FieldAccess expr = (FieldAccess) toMatch;

            return expr.getClassName().equals(className) &&
                    expr.getFieldName().equals(fieldName);
        }

    }

    public static class CatchClauseMatcher extends Matcher {

        private String exceptionType;
        private boolean isFinallyClause;

        public CatchClauseMatcher(Class<?> exceptionType, boolean isFinallyClause) {
            this(exceptionType.getName(), isFinallyClause);
        }

        public CatchClauseMatcher(String exceptionType, boolean isFinallyClause) {
            super(Expectation.CATCH_CLAUSE);

            this.exceptionType = exceptionType;
            this.isFinallyClause = isFinallyClause;
        }

        public boolean match(Expr toMatch) {
            Handler expr = (Handler) toMatch;

            boolean result = false;

            try {
                result = expr.getType().getName().equals(exceptionType) &&
                        expr.isFinally() == isFinallyClause;
            } catch (NotFoundException e) {
                // this is allowed to happen so eat it
            }

            return result;
        }

    }

    public static class InstanceOfMatcher extends Matcher {

        private String comparedToType;

        public InstanceOfMatcher(Class<?> clazz) {
            this(clazz.getName());
        }

        public InstanceOfMatcher(String comparedToType) {
            super(Expectation.INSTANCEOF);

            this.comparedToType = comparedToType;
        }

        public boolean match(Expr toMatch) {
            Instanceof expr = (Instanceof) toMatch;

            boolean result = false;

            try {
                result = expr.getType().getName().equals(comparedToType);
            } catch (NotFoundException e) {
                // this is allowed to happen so eat it
            }

            return result;
        }

    }

    public static class MethodCallMatcher extends Matcher {

        private String className, methodName;

        public MethodCallMatcher(Class<?> clazz, String methodName) {
            this(clazz.getName(), methodName);
        }

        public MethodCallMatcher(String className, String methodName) {
            super(Expectation.METHOD_CALL);

            this.className = className;
            this.methodName = methodName;
        }

        public boolean match(Expr toMatch) {
            MethodCall expr = (MethodCall) toMatch;

            return expr.getClassName().equals(className) &&
                    expr.getMethodName().equals(methodName);
        }

    }

    public static class NewArrayMatcher extends Matcher {

        private String className;

        public NewArrayMatcher(Class<?> clazz) {
            this(clazz.getName());
        }

        public NewArrayMatcher(String className) {
            super(Expectation.ARRAY_CREATION);

            this.className = className;
        }

        public boolean match(Expr toMatch) {
            NewArray expr = (NewArray) toMatch;

            boolean result = false;

            try {
                result = expr.getComponentType().getName().equals(className);
            } catch (NotFoundException e) {
                // this is allowed to happen so eat it
            }

            return result;
        }

    }

    public static class NewExprMatcher extends Matcher {

        private String className;

        public NewExprMatcher(Class<?> clazz) {
            this(clazz.getName());
        }

        public NewExprMatcher(String className) {
            super(Expectation.NEW_EXPRESSION);

            this.className = className;
        }

        public boolean match(Expr toMatch) {
            NewExpr expr = (NewExpr) toMatch;

            return expr.getClassName().equals(className);
        }

    }

}
