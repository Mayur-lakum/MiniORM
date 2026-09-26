package com.miniorm.core;

import com.miniorm.annotations.Column;
import com.miniorm.annotations.Entity;
import com.miniorm.annotations.Id;
import com.miniorm.annotations.Table;
import com.miniorm.exception.MiniORMException;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public final class ReflectionUtil
{
    private ReflectionUtil()
    {
    }

    // Get table name from @Table
    public static String getTableName(Class<?> clazz)
    {
        if (clazz == null)
        {
            throw new MiniORMException("Entity class cannot be null");
        }

        if (!clazz.isAnnotationPresent(Entity.class))
        {
            throw new MiniORMException(
                    "Class must have @Entity annotation"
            );
        }

        Table table =
                clazz.getAnnotation(Table.class);

        if (table == null)
        {
            throw new MiniORMException(
                    "Class must have @Table annotation"
            );
        }

        return table.name();
    }

    // Get fields that have @Column
    public static Field[] getFields(Class<?> clazz)
    {
        Field[] allFields =
                clazz.getDeclaredFields();

        Set<Field> fields =
                new HashSet<>();

        for (Field field : allFields)
        {
            if (field.isAnnotationPresent(Column.class))
            {
                fields.add(field);
            }
        }

        return fields.toArray(new Field[0]);
    }

    // Get database column name
    public static String getColumnName(Field field)
    {
        Column column =
                field.getAnnotation(Column.class);

        if (column == null)
        {
            throw new MiniORMException(
                    "Field must have @Column annotation"
            );
        }

        if (column.name().isBlank())
        {
            return field.getName();
        }

        return column.name();
    }

    // Get all valid column names
    public static Set<String> getValidColumnNames(
            Class<?> clazz)
    {
        Set<String> columns =
                new HashSet<>();

        for (Field field : getFields(clazz))
        {
            columns.add(
                    getColumnName(field)
            );
        }

        return columns;
    }

    // Check whether column exists
    public static String validateColumn(
            Class<?> clazz,
            String column)
    {
        if (!getValidColumnNames(clazz)
                .contains(column))
        {
            throw new MiniORMException(
                    "Invalid column: " + column
            );
        }

        return column;
    }

    // Check primary key
    public static boolean isPrimaryKey(Field field)
    {
        return field.isAnnotationPresent(Id.class);
    }

    // Get primary key field
    public static Field getPrimaryKeyField(
            Class<?> clazz)
    {
        for (Field field :
                clazz.getDeclaredFields())
        {
            if (isPrimaryKey(field))
            {
                return field;
            }
        }

        throw new MiniORMException(
                "No @Id field found in "
                        + clazz.getSimpleName()
        );
    }

    // Read field value
    public static Object getFieldValue(
            Object object,
            Field field)
    {
        try
        {
            field.setAccessible(true);

            return field.get(object);
        }
        catch (IllegalAccessException e)
        {
            throw new MiniORMException(
                    "Cannot read field: "
                            + field.getName(),
                    e
            );
        }
    }

    // Set field value
    public static void setFieldValue(
            Object object,
            Field field,
            Object value)
    {
        try
        {
            field.setAccessible(true);

            // Handle database numeric types
            if (value instanceof Number)
            {
                Class<?> type =
                        field.getType();

                Number number =
                        (Number) value;

                if (type == int.class
                        || type == Integer.class)
                {
                    field.set(
                            object,
                            number.intValue()
                    );
                    return;
                }

                if (type == long.class
                        || type == Long.class)
                {
                    field.set(
                            object,
                            number.longValue()
                    );
                    return;
                }

                if (type == double.class
                        || type == Double.class)
                {
                    field.set(
                            object,
                            number.doubleValue()
                    );
                    return;
                }

                if (type == float.class
                        || type == Float.class)
                {
                    field.set(
                            object,
                            number.floatValue()
                    );
                    return;
                }
            }

            field.set(object, value);
        }
        catch (IllegalAccessException e)
        {
            throw new MiniORMException(
                    "Cannot set field: "
                            + field.getName(),
                    e
            );
        }
    }
}