package com.miniorm.core;

import com.miniorm.config.DBConnection;
import com.miniorm.exception.MiniORMException;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class EntityManager
{
    private EntityManager()
    {
    }

    // ==================== SAVE ====================

    public static void save(Object object)
    {
        if (object == null)
        {
            throw new MiniORMException(
                    "Entity cannot be null"
            );
        }

        Class<?> clazz = object.getClass();

        String query =
                SQLGenerator.generateInsertQuery(clazz);

        Object[] values =
                SQLGenerator.getInsertValues(object);

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             query,
                             Statement.RETURN_GENERATED_KEYS))
        {
            setValues(statement, values);

            int rows =
                    statement.executeUpdate();

            setGeneratedId(object, statement);

            System.out.println(
                    "Inserted " + rows + " row(s)."
            );
        }
        catch (SQLException e)
        {
            throw new MiniORMException(
                    "Failed to save entity",
                    e
            );
        }
    }

    // ==================== SAVE ALL ====================

    public static <T> void saveAll(
            List<T> objects)
    {
        if (objects == null || objects.isEmpty())
        {
            return;
        }

        Class<?> clazz =
                objects.get(0).getClass();

        for (T object : objects)
        {
            if (object == null)
            {
                throw new MiniORMException(
                        "Entity cannot be null"
                );
            }
        }

        String query =
                SQLGenerator.generateInsertQuery(clazz);

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             query,
                             Statement.RETURN_GENERATED_KEYS))
        {
            connection.setAutoCommit(false);

            for (T object : objects)
            {
                setValues(
                        statement,
                        SQLGenerator.getInsertValues(object)
                );

                statement.addBatch();
            }

            statement.executeBatch();

            setGeneratedIds(
                    objects,
                    statement
            );

            connection.commit();

            System.out.println(
                    "Inserted "
                            + objects.size()
                            + " row(s) using batch."
            );
        }
        catch (SQLException e)
        {
            throw new MiniORMException(
                    "Failed to save batch",
                    e
            );
        }
    }

    // ==================== FIND BY ID ====================

    public static <T> T findById(
            Class<T> clazz,
            Object id)
    {
        if (clazz == null || id == null)
        {
            throw new MiniORMException(
                    "Class and ID cannot be null"
            );
        }

        Field primaryKey =
                ReflectionUtil.getPrimaryKeyField(clazz);

        String query =
                "SELECT * FROM "
                        + ReflectionUtil.getTableName(clazz)
                        + " WHERE "
                        + ReflectionUtil.getColumnName(primaryKey)
                        + " = ?";

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(query))
        {
            statement.setObject(1, id);

            try (ResultSet resultSet =
                         statement.executeQuery())
            {
                if (resultSet.next())
                {
                    return mapRow(
                            clazz,
                            resultSet
                    );
                }
            }
        }
        catch (Exception e)
        {
            throw new MiniORMException(
                    "Failed to find entity",
                    e
            );
        }

        return null;
    }

    // ==================== FIND ALL ====================

    public static <T> List<T> findAll(
            Class<T> clazz)
    {
        if (clazz == null)
        {
            throw new MiniORMException(
                    "Entity class cannot be null"
            );
        }

        List<T> result =
                new ArrayList<>();

        String query =
                SQLGenerator.generateFindAllQuery(clazz);

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(query);

             ResultSet resultSet =
                     statement.executeQuery())
        {
            while (resultSet.next())
            {
                result.add(
                        mapRow(
                                clazz,
                                resultSet
                        )
                );
            }
        }
        catch (Exception e)
        {
            throw new MiniORMException(
                    "Failed to fetch entities",
                    e
            );
        }

        return result;
    }

    // ==================== FIND BY COLUMN ====================

    public static <T> List<T> findByColumn(
            Class<T> clazz,
            String column,
            Object value)
    {
        return findWhere(
                clazz,
                column,
                QueryOperator.EQUALS,
                value
        );
    }

    // ==================== FIND WHERE ====================

    public static <T> List<T> findWhere(
            Class<T> clazz,
            String column,
            QueryOperator operator,
            Object value)
    {
        if (clazz == null || operator == null)
        {
            throw new MiniORMException(
                    "Invalid query parameters"
            );
        }

        String validColumn =
                ReflectionUtil.validateColumn(
                        clazz,
                        column
                );

        String query =
                SQLGenerator.generateFindByColumnQuery(
                        clazz,
                        validColumn,
                        operator
                );

        List<T> result =
                new ArrayList<>();

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(query))
        {
            statement.setObject(1, value);

            try (ResultSet resultSet =
                         statement.executeQuery())
            {
                while (resultSet.next())
                {
                    result.add(
                            mapRow(
                                    clazz,
                                    resultSet
                            )
                    );
                }
            }
        }
        catch (Exception e)
        {
            throw new MiniORMException(
                    "Failed to execute query",
                    e
            );
        }

        return result;
    }

    // ==================== UPDATE ====================

    public static void update(Object object)
    {
        if (object == null)
        {
            throw new MiniORMException(
                    "Entity cannot be null"
            );
        }

        Class<?> clazz =
                object.getClass();

        String query =
                SQLGenerator.generateUpdateQuery(clazz);

        Field[] fields =
                ReflectionUtil.getFields(clazz);

        Field primaryKey =
                ReflectionUtil.getPrimaryKeyField(clazz);

        Object id =
                ReflectionUtil.getFieldValue(
                        object,
                        primaryKey
                );

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(query))
        {
            int index = 1;

            for (Field field : fields)
            {
                if (ReflectionUtil.isPrimaryKey(field))
                {
                    continue;
                }

                Object value =
                        ReflectionUtil.getFieldValue(
                                object,
                                field
                        );

                statement.setObject(
                        index++,
                        value
                );
            }

            statement.setObject(
                    index,
                    id
            );

            int rows =
                    statement.executeUpdate();

            System.out.println(
                    "Updated " + rows + " row(s)."
            );
        }
        catch (SQLException e)
        {
            throw new MiniORMException(
                    "Failed to update entity",
                    e
            );
        }
    }

    // ==================== DELETE ====================

    public static <T> void delete(
            Class<T> clazz,
            Object id)
    {
        if (clazz == null || id == null)
        {
            throw new MiniORMException(
                    "Class and ID cannot be null"
            );
        }

        String query =
                SQLGenerator.generateDeleteQuery(clazz);

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(query))
        {
            statement.setObject(1, id);

            int rows =
                    statement.executeUpdate();

            System.out.println(
                    "Deleted " + rows + " row(s)."
            );
        }
        catch (SQLException e)
        {
            throw new MiniORMException(
                    "Failed to delete entity",
                    e
            );
        }
    }

    // ==================== MAP RESULT ====================

    private static <T> T mapRow(
            Class<T> clazz,
            ResultSet resultSet)
            throws Exception
    {
        T object =
                clazz.getDeclaredConstructor()
                        .newInstance();

        for (Field field :
                ReflectionUtil.getFields(clazz))
        {
            String column =
                    ReflectionUtil.getColumnName(field);

            Object value =
                    resultSet.getObject(column);

            ReflectionUtil.setFieldValue(
                    object,
                    field,
                    value
            );
        }

        return object;
    }

    // ==================== SET VALUES ====================

    private static void setValues(
            PreparedStatement statement,
            Object[] values)
            throws SQLException
    {
        for (int i = 0; i < values.length; i++)
        {
            statement.setObject(
                    i + 1,
                    values[i]
            );
        }
    }

    // ==================== GENERATED ID ====================

    private static void setGeneratedId(
            Object object,
            PreparedStatement statement)
            throws SQLException
    {
        try (ResultSet keys =
                     statement.getGeneratedKeys())
        {
            if (keys.next())
            {
                Field primaryKey =
                        ReflectionUtil.getPrimaryKeyField(
                                object.getClass()
                        );

                ReflectionUtil.setFieldValue(
                        object,
                        primaryKey,
                        keys.getObject(1)
                );
            }
        }
    }

    private static <T> void setGeneratedIds(
            List<T> objects,
            PreparedStatement statement)
            throws SQLException
    {
        Field primaryKey =
                ReflectionUtil.getPrimaryKeyField(
                        objects.get(0).getClass()
                );

        try (ResultSet keys =
                     statement.getGeneratedKeys())
        {
            int index = 0;

            while (keys.next()
                    && index < objects.size())
            {
                ReflectionUtil.setFieldValue(
                        objects.get(index),
                        primaryKey,
                        keys.getObject(1)
                );

                index++;
            }
        }
    }
}