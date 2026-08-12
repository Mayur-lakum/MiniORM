package com.miniorm.core;

import com.miniorm.exception.MiniORMException;

import java.lang.reflect.Field;

public final class SQLGenerator
{
    private SQLGenerator()
    {
    }

    public static String generateInsertQuery(Class<?> clazz)
    {
        validateClass(clazz);

        String tableName =
                ReflectionUtil.getTableName(clazz);

        Field[] fields =
                getInsertFields(clazz);

        if (fields.length == 0)
        {
            throw new MiniORMException(
                    "Entity " + clazz.getSimpleName()
                            + " has no insertable fields."
            );
        }

        StringBuilder columns =
                new StringBuilder();

        StringBuilder values =
                new StringBuilder();

        for (Field field : fields)
        {
            columns.append(
                    ReflectionUtil.getColumnName(field)
            ).append(",");

            values.append("?,");
        }

        removeLastCharacter(columns);
        removeLastCharacter(values);

        return "INSERT INTO "
                + tableName
                + " ("
                + columns
                + ") VALUES ("
                + values
                + ")";
    }

    public static Object[] getInsertValues(
            Object object)
    {
        if (object == null)
        {
            throw new MiniORMException(
                    "Entity object cannot be null."
            );
        }

        Field[] fields =
                getInsertFields(object.getClass());

        Object[] values =
                new Object[fields.length];

        for (int i = 0; i < fields.length; i++)
        {
            values[i] =
                    ReflectionUtil.getFieldValue(
                            object,
                            fields[i]
                    );
        }

        return values;
    }

    public static String generateUpdateQuery(
            Class<?> clazz)
    {
        validateClass(clazz);

        String tableName =
                ReflectionUtil.getTableName(clazz);

        Field[] fields =
                ReflectionUtil.getFields(clazz);

        Field primaryKey =
                ReflectionUtil.getPrimaryKeyField(clazz);

        StringBuilder query =
                new StringBuilder();

        query.append("UPDATE ")
                .append(tableName)
                .append(" SET ");

        int updateFieldCount = 0;

        for (Field field : fields)
        {
            if (ReflectionUtil.isPrimaryKey(field))
            {
                continue;
            }

            query.append(
                    ReflectionUtil.getColumnName(field)
            ).append("=?,");

            updateFieldCount++;
        }

        if (updateFieldCount == 0)
        {
            throw new MiniORMException(
                    "Entity " + clazz.getSimpleName()
                            + " has no fields to update."
            );
        }

        removeLastCharacter(query);

        query.append(" WHERE ")
                .append(
                        ReflectionUtil.getColumnName(
                                primaryKey
                        )
                )
                .append("=?");

        return query.toString();
    }

    public static String generateDeleteQuery(
            Class<?> clazz)
    {
        validateClass(clazz);

        String tableName =
                ReflectionUtil.getTableName(clazz);

        Field primaryKey =
                ReflectionUtil.getPrimaryKeyField(clazz);

        return "DELETE FROM "
                + tableName
                + " WHERE "
                + ReflectionUtil.getColumnName(primaryKey)
                + " = ?";
    }

    public static String generateFindAllQuery(
            Class<?> clazz)
    {
        validateClass(clazz);

        return "SELECT * FROM "
                + ReflectionUtil.getTableName(clazz);
    }

    public static String generateFindByColumnQuery(
            Class<?> clazz,
            String column,
            QueryOperator operator)
    {
        validateClass(clazz);

        if (operator == null)
        {
            throw new MiniORMException(
                    "Query operator cannot be null."
            );
        }

        String validatedColumn =
                ReflectionUtil.validateColumn(
                        clazz,
                        column
                );

        return "SELECT * FROM "
                + ReflectionUtil.getTableName(clazz)
                + " WHERE "
                + validatedColumn
                + " "
                + operator.toSql()
                + " ?";
    }

    public static String generateFindByColumnQuery(
            Class<?> clazz,
            String column)
    {
        return generateFindByColumnQuery(
                clazz,
                column,
                QueryOperator.EQUALS
        );
    }

    private static Field[] getInsertFields(
            Class<?> clazz)
    {
        validateClass(clazz);

        return java.util.Arrays.stream(
                        ReflectionUtil.getFields(clazz)
                )
                .filter(field ->
                        !ReflectionUtil.isPrimaryKey(field))
                .toArray(Field[]::new);
    }

    private static void validateClass(
            Class<?> clazz)
    {
        if (clazz == null)
        {
            throw new MiniORMException(
                    "Entity class cannot be null."
            );
        }
    }

    private static void removeLastCharacter(
            StringBuilder builder)
    {
        if (builder.length() > 0)
        {
            builder.deleteCharAt(
                    builder.length() - 1
            );
        }
    }
}