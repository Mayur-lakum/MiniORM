package com.miniorm.core;

import com.miniorm.annotations.Column;
import com.miniorm.annotations.Entity;
import com.miniorm.annotations.Id;
import com.miniorm.annotations.Table;
import com.miniorm.exception.MiniORMException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ReflectionUtil
{
    // Cache reflection metadata to avoid repeated field scanning.
    private static final Map<Class<?>, Field[]> FIELD_CACHE =
            new ConcurrentHashMap<>();

    // Cache primary key fields.
    private static final Map<Class<?>, Field> PK_CACHE =
            new ConcurrentHashMap<>();

    // Cache valid database column names.
    private static final Map<Class<?>, Set<String>> COLUMN_NAME_CACHE =
            new ConcurrentHashMap<>();

    private ReflectionUtil()
    {
    }

    public static String getTableName(Class<?> clazz)
    {
        if (clazz == null)
        {
            throw new MiniORMException(
                    "Entity class cannot be null."
            );
        }

        if (!clazz.isAnnotationPresent(Entity.class))
        {
            throw new MiniORMException(
                    "Class " + clazz.getName()
                            + " is missing @Entity annotation."
            );
        }

        if (!clazz.isAnnotationPresent(Table.class))
        {
            throw new MiniORMException(
                    "Entity " + clazz.getName()
                            + " is missing @Table annotation."
            );
        }

        String tableName =
                clazz.getAnnotation(Table.class).name();

        if (tableName == null || tableName.isBlank())
        {
            throw new MiniORMException(
                    "Entity " + clazz.getName()
                            + " has an empty @Table name."
            );
        }

        return tableName;
    }

    public static Field[] getFields(Class<?> clazz)
    {
        if (clazz == null)
        {
            throw new MiniORMException(
                    "Entity class cannot be null."
            );
        }

        return FIELD_CACHE.computeIfAbsent(
                clazz,
                c -> Arrays.stream(c.getDeclaredFields())
                        .filter(field ->
                                field.isAnnotationPresent(Column.class))
                        .toArray(Field[]::new)
        );
    }

    public static String getColumnName(Field field)
    {
        if (field == null)
        {
            throw new MiniORMException(
                    "Field cannot be null."
            );
        }

        if (!field.isAnnotationPresent(Column.class))
        {
            throw new MiniORMException(
                    "Field '" + field.getName()
                            + "' is missing @Column annotation."
            );
        }

        String name =
                field.getAnnotation(Column.class).name();

        return (name == null || name.isBlank())
                ? field.getName()
                : name;
    }

    public static Set<String> getValidColumnNames(
            Class<?> clazz)
    {
        return COLUMN_NAME_CACHE.computeIfAbsent(
                clazz,
                c -> Arrays.stream(getFields(c))
                        .map(ReflectionUtil::getColumnName)
                        .collect(Collectors.toUnmodifiableSet())
        );
    }

    public static String validateColumn(
            Class<?> clazz,
            String column)
    {
        if (column == null || column.isBlank())
        {
            throw new MiniORMException(
                    "Column name cannot be null or blank."
            );
        }

        Set<String> validColumns =
                getValidColumnNames(clazz);

        if (!validColumns.contains(column))
        {
            throw new MiniORMException(
                    "Unknown column '" + column
                            + "' for entity "
                            + clazz.getSimpleName()
                            + ". Valid columns: "
                            + validColumns
            );
        }

        return column;
    }

    public static boolean isPrimaryKey(Field field)
    {
        return field != null
                && field.isAnnotationPresent(Id.class);
    }

    public static Object getFieldValue(
            Object object,
            Field field)
    {
        if (object == null)
        {
            throw new MiniORMException(
                    "Entity object cannot be null."
            );
        }

        if (field == null)
        {
            throw new MiniORMException(
                    "Field cannot be null."
            );
        }

        try
        {
            field.setAccessible(true);
            return field.get(object);
        }
        catch (IllegalAccessException e)
        {
            throw new MiniORMException(
                    "Failed to read field '"
                            + field.getName() + "'.",
                    e
            );
        }
    }

    public static Field getPrimaryKeyField(
            Class<?> clazz)
    {
        if (clazz == null)
        {
            throw new MiniORMException(
                    "Entity class cannot be null."
            );
        }

        return PK_CACHE.computeIfAbsent(
                clazz,
                c ->
                {
                    Field primaryKey = null;

                    for (Field field : c.getDeclaredFields())
                    {
                        if (isPrimaryKey(field))
                        {
                            if (primaryKey != null)
                            {
                                throw new MiniORMException(
                                        "Entity "
                                                + c.getSimpleName()
                                                + " contains multiple "
                                                + "@Id fields. "
                                                + "MiniORM requires exactly "
                                                + "one primary key."
                                );
                            }

                            primaryKey = field;
                        }
                    }

                    if (primaryKey == null)
                    {
                        throw new MiniORMException(
                                "Entity "
                                        + c.getSimpleName()
                                        + " does not contain "
                                        + "an @Id field."
                        );
                    }

                    return primaryKey;
                }
        );
    }

    public static void setFieldValue(
            Object object,
            Field field,
            Object value)
    {
        if (object == null)
        {
            throw new MiniORMException(
                    "Entity object cannot be null."
            );
        }

        if (field == null)
        {
            throw new MiniORMException(
                    "Field cannot be null."
            );
        }

        try
        {
            field.setAccessible(true);

            if (value == null)
            {
                if (field.getType().isPrimitive())
                {
                    throw new MiniORMException(
                            "Cannot assign null to primitive field '"
                                    + field.getName() + "'."
                    );
                }

                field.set(object, null);
                return;
            }

            Class<?> type = field.getType();

            if (type == int.class || type == Integer.class)
            {
                field.set(object, ((Number) value).intValue());
            }
            else if (type == long.class || type == Long.class)
            {
                field.set(object, ((Number) value).longValue());
            }
            else if (type == double.class
                    || type == Double.class)
            {
                field.set(object, ((Number) value).doubleValue());
            }
            else if (type == float.class
                    || type == Float.class)
            {
                field.set(object, ((Number) value).floatValue());
            }
            else
            {
                field.set(object, value);
            }
        }
        catch (IllegalAccessException e)
        {
            throw new MiniORMException(
                    "Failed to write field '"
                            + field.getName() + "'.",
                    e
            );
        }
        catch (IllegalArgumentException e)
        {
            throw new MiniORMException(
                    "Invalid value for field '"
                            + field.getName() + "'.",
                    e
            );
        }
    }
}