package com.miniorm.core;

import com.miniorm.exception.MiniORMException;

import java.lang.reflect.Field;

public final class SQLGenerator
{
    private SQLGenerator()
    {
    }

    // INSERT query
    public static String generateInsertQuery(
            Class<?> clazz)
    {
        String tableName =
                ReflectionUtil.getTableName(clazz);

        Field[] fields =
                getInsertFields(clazz);

        StringBuilder columns =
                new StringBuilder();

        StringBuilder values =
                new StringBuilder();

        for (Field field : fields)
        {
            columns.append(
                    ReflectionUtil.getColumnName(field)
            ).append(", ");

            values.append("?, ");
        }

        // Remove last comma
        columns.setLength(columns.length() - 2);
        values.setLength(values.length() - 2);

        return "INSERT INTO "
                + tableName
                + " ("
                + columns
                + ") VALUES ("
                + values
                + ")";
    }

    // Get values for INSERT
    public static Object[] getInsertValues(
            Object object)
    {
        if (object == null)
        {
            throw new MiniORMException(
                    "Entity cannot be null"
            );
        }

        Field[] fields =
                getInsertFields(
                        object.getClass()
                );

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

    // UPDATE query
    public static String generateUpdateQuery(
            Class<?> clazz)
    {
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

        for (Field field : fields)
        {
            if (ReflectionUtil.isPrimaryKey(field))
            {
                continue;
            }

            query.append(
                    ReflectionUtil.getColumnName(field)
            ).append(" = ?, ");
        }

        // Remove last comma
        query.setLength(query.length() - 2);

        query.append(" WHERE ")
                .append(
                        ReflectionUtil.getColumnName(
                                primaryKey
                        )
                )
                .append(" = ?");

        return query.toString();
    }

    // DELETE query
    public static String generateDeleteQuery(
            Class<?> clazz)
    {
        String tableName =
                ReflectionUtil.getTableName(clazz);

        Field primaryKey =
                ReflectionUtil.getPrimaryKeyField(clazz);

        return "DELETE FROM "
                + tableName
                + " WHERE "
                + ReflectionUtil.getColumnName(
                primaryKey
        )
                + " = ?";
    }

    // SELECT all records
    public static String generateFindAllQuery(
            Class<?> clazz)
    {
        return "SELECT * FROM "
                + ReflectionUtil.getTableName(clazz);
    }

    // SELECT with WHERE condition
    public static String generateFindByColumnQuery(
            Class<?> clazz,
            String column,
            QueryOperator operator)
    {
        if (operator == null)
        {
            throw new MiniORMException(
                    "Query operator cannot be null"
            );
        }

        String validColumn =
                ReflectionUtil.validateColumn(
                        clazz,
                        column
                );

        return "SELECT * FROM "
                + ReflectionUtil.getTableName(clazz)
                + " WHERE "
                + validColumn
                + " "
                + operator.toSql()
                + " ?";
    }

    // Simple WHERE column = value
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

    // Get fields used during INSERT
    private static Field[] getInsertFields(
            Class<?> clazz)
    {
        Field[] allFields =
                ReflectionUtil.getFields(clazz);

        int count = 0;

        for (Field field : allFields)
        {
            if (!ReflectionUtil.isPrimaryKey(field))
            {
                count++;
            }
        }

        Field[] insertFields =
                new Field[count];

        int index = 0;

        for (Field field : allFields)
        {
            if (!ReflectionUtil.isPrimaryKey(field))
            {
                insertFields[index++] = field;
            }
        }

        if (insertFields.length == 0)
        {
            throw new MiniORMException(
                    "No fields available for INSERT"
            );
        }

        return insertFields;
    }
}