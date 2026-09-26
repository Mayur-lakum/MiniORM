package com.miniorm.core;

public enum QueryOperator
{
    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    GREATER_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_OR_EQUAL("<="),
    LIKE("LIKE");

    private final String sql;

    QueryOperator(String sql)
    {
        this.sql = sql;
    }

    public String toSql()
    {
        return sql;
    }
}