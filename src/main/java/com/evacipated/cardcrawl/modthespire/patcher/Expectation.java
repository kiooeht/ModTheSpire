package com.evacipated.cardcrawl.modthespire.patcher;

public enum Expectation {
    TYPE_CAST, CONSTRUCTOR_CALL, FIELD_ACCESS,
    CATCH_CLAUSE, INSTANCEOF, METHOD_CALL,
    ARRAY_CREATION, NEW_EXPRESSION
}
