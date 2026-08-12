package com.miniorm.core;

/**
 * Represents the comparison operators supported by MiniORM
 * while building dynamic SQL WHERE clauses.
 *
 * <p>Using an enum instead of raw Strings prevents invalid
 * SQL operators and improves type safety. It also protects
 * against SQL injection because only predefined operators
 * can be used.
 *
 * <p>Example:
 * <pre>{@code
 * repository.findWhere(
 *         "price",
 *         QueryOperator.GREATER_THAN,
 *         1000
 * );
 * }</pre>
 *
 * Supported Operators:
 * <ul>
 *     <li>=</li>
 *     <li>!=</li>
 *     <li>></li>
 *     <li>>=</li>
 *     <li><</li>
 *     <li><=</li>
 *     <li>LIKE</li>
 * </ul>
 *
 * @author Mayur Lakum
 */
public enum QueryOperator
{
    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    GREATER_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_OR_EQUAL("<="),
    LIKE("LIKE");

    /**
     * SQL representation of the operator.
     */
    private final String sql;

    QueryOperator(String sql)
    {
        this.sql = sql;
    }

    /**
     * Returns the SQL symbol associated with this operator.
     *
     * @return SQL operator
     */
    public String toSql()
    {
        return sql;
    }

    @Override
    public String toString()
    {
        return sql;
    }
}