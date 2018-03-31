package com.evacipated.cardcrawl.modthespire.lib;

import javassist.NotFoundException;
import javassist.expr.Cast;
import javassist.expr.ConstructorCall;
import javassist.expr.Expr;
import javassist.expr.MethodCall;
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
	
	public class TypeCastMatcher extends Matcher {
		
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
	
	public class ConstructorCallMatcher extends Matcher {

		private String className, methodName;
		private boolean checkMethodName;
		
		public ConstructorCallMatcher(String className) {
			super(Expectation.CONSTRUCTOR_CALL);
			
			this.className = className;
			this.checkMethodName = false;
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
	
	public class MethodMatcher extends Matcher {

		private String className, methodName;
		
		public MethodMatcher(String className, String methodName) {
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
	
	public class NewExprMatcher extends Matcher {

		private String className;
		
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
