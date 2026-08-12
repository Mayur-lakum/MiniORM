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

    /**
     * Saves a single entity into the database.
     *
     * <p>The generated primary key is automatically assigned
     * back to the entity object.
     *
     * @param object entity to save
     */
    public static void save(Object object)
    {
        validateObject(object);

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
            bindValues(statement, values);

            int rows =
                    statement.executeUpdate();

            assignGeneratedId(
                    object,
                    statement
            );

            System.out.printf(
                    "✅ %d row(s) inserted.%n",
                    rows
            );
        }
        catch (SQLException e)
        {
            throw new MiniORMException(
                    "Failed to save entity: "
                            + clazz.getSimpleName(),
                    e
            );
        }
    }

    /**
     * Saves multiple entities using JDBC batch processing.
     *
     * <p>The operation runs inside a transaction. If any database
     * error occurs, the transaction is rolled back.
     *
     * <p>Generated primary keys are retrieved and assigned back
     * to the corresponding entity objects.
     *
     * @param objects entities to save
     */
    public static <T> void saveAll(List<T> objects)
    {
        if (objects == null || objects.isEmpty())
        {
            return;
        }

        validateBatch(objects);

        Class<?> clazz =
                objects.get(0).getClass();

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
                bindValues(
                        statement,
                        SQLGenerator.getInsertValues(object)
                );

                statement.addBatch();
            }

            int[] results =
                    statement.executeBatch();

            assignGeneratedIds(
                    objects,
                    statement
            );

            connection.commit();

            System.out.printf(
                    "✅ %d row(s) inserted using batch.%n",
                    results.length
            );
        }
        catch (SQLException e)
        {
            throw new MiniORMException(
                    "Failed to batch save entities of type "
                            + clazz.getSimpleName(),
                    e
            );
        }
        catch (RuntimeException e)
        {
            throw new MiniORMException(
                    "Failed to batch save entities of type "
                            + clazz.getSimpleName(),
                    e
            );
        }
    }

    /**
     * Finds an entity by its primary key.
     *
     * @param clazz entity class
     * @param id primary key value
     * @param <T> entity type
     * @return mapped entity, or null if not found
     */
    public static <T> T findById(
            Class<T> clazz,
            Object id)
    {
        if (clazz == null)
        {
            throw new MiniORMException(
                    "Entity class cannot be null."
            );
        }

        if (id == null)
        {
            throw new MiniORMException(
                    "Primary key value cannot be null."
            );
        }

        String tableName =
                ReflectionUtil.getTableName(clazz);

        Field primaryKey =
                ReflectionUtil.getPrimaryKeyField(clazz);

        String columnName =
                ReflectionUtil.getColumnName(primaryKey);

        String query =
                "SELECT * FROM "
                        + tableName
                        + " WHERE "
                        + columnName
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
                    "Failed to find "
                            + clazz.getSimpleName()
                            + " with id "
                            + id,
                    e
            );
        }

        return null;
    }

    /**
     * Updates an existing entity using its primary key.
     *
     * @param object entity to update
     */
    public static void update(Object object)
    {
        validateObject(object);

        Class<?> clazz = object.getClass();

        String query =
                SQLGenerator.generateUpdateQuery(clazz);

        Field[] fields =
                ReflectionUtil.getFields(clazz);

        Field primaryKey =
                ReflectionUtil.getPrimaryKeyField(clazz);

        Object primaryKeyValue =
                ReflectionUtil.getFieldValue(
                        object,
                        primaryKey
                );

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(query))
        {
            int parameterIndex = 1;

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
                        parameterIndex++,
                        value
                );
            }

            statement.setObject(
                    parameterIndex,
                    primaryKeyValue
            );

            int rows =
                    statement.executeUpdate();

            System.out.printf(
                    "✅ %d row(s) updated.%n",
                    rows
            );
        }
        catch (SQLException e)
        {
            throw new MiniORMException(
                    "Failed to update entity: "
                            + clazz.getSimpleName(),
                    e
            );
        }
    }

    /**
     * Deletes an entity by its primary key.
     *
     * @param clazz entity class
     * @param id primary key value
     * @param <T> entity type
     */
    public static <T> void delete(
            Class<T> clazz,
            Object id)
    {
        if (clazz == null)
        {
            throw new MiniORMException(
                    "Entity class cannot be null."
            );
        }

        if (id == null)
        {
            throw new MiniORMException(
                    "Primary key value cannot be null."
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

            System.out.printf(
                    "✅ %d row(s) deleted.%n",
                    rows
            );
        }
        catch (SQLException e)
        {
            throw new MiniORMException(
                    "Failed to delete "
                            + clazz.getSimpleName()
                            + " with id "
                            + id,
                    e
            );
        }
    }

    /**
     * Retrieves all records for an entity.
     *
     * @param clazz entity class
     * @param <T> entity type
     * @return list of mapped entities
     */
    public static <T> List<T> findAll(
            Class<T> clazz)
    {
        if (clazz == null)
        {
            throw new MiniORMException(
                    "Entity class cannot be null."
            );
        }

        List<T> objects =
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
                objects.add(
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
                    "Failed to retrieve "
                            + clazz.getSimpleName()
                            + " records.",
                    e
            );
        }

        return objects;
    }

    /**
     * Finds entities where a column equals the supplied value.
     *
     * @param clazz entity class
     * @param column column name
     * @param value value to search for
     * @param <T> entity type
     * @return matching entities
     */
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

    /**
     * Executes a dynamic WHERE query.
     *
     * @param clazz entity class
     * @param column column name
     * @param operator comparison operator
     * @param value comparison value
     * @param <T> entity type
     * @return matching entities
     */
    public static <T> List<T> findWhere(
            Class<T> clazz,
            String column,
            QueryOperator operator,
            Object value)
    {
        if (clazz == null)
        {
            throw new MiniORMException(
                    "Entity class cannot be null."
            );
        }

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

        List<T> objects =
                new ArrayList<>();

        String query =
                SQLGenerator.generateFindByColumnQuery(
                        clazz,
                        validatedColumn,
                        operator
                );

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
                    objects.add(
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
                    "Failed to query "
                            + clazz.getSimpleName()
                            + " where "
                            + validatedColumn
                            + " "
                            + operator,
                    e
            );
        }

        return objects;
    }

    /**
     * Binds values to a PreparedStatement.
     *
     * @param statement prepared statement
     * @param values values to bind
     * @throws SQLException if binding fails
     */
    private static void bindValues(
            PreparedStatement statement,
            Object[] values)
            throws SQLException
    {
        if (values == null)
        {
            return;
        }

        for (int i = 0; i < values.length; i++)
        {
            statement.setObject(
                    i + 1,
                    values[i]
            );
        }
    }

    /**
     * Assigns a generated primary key to a single entity.
     *
     * @param object saved entity
     * @param statement prepared statement
     * @throws SQLException if generated key retrieval fails
     */
    private static void assignGeneratedId(
            Object object,
            PreparedStatement statement)
            throws SQLException
    {
        try (ResultSet generatedKeys =
                     statement.getGeneratedKeys())
        {
            if (generatedKeys.next())
            {
                Field primaryKey =
                        ReflectionUtil.getPrimaryKeyField(
                                object.getClass()
                        );

                ReflectionUtil.setFieldValue(
                        object,
                        primaryKey,
                        generatedKeys.getObject(1)
                );
            }
        }
    }

    /**
     * Assigns generated primary keys to entities inserted
     * through batch processing.
     *
     * @param objects saved entities
     * @param statement prepared statement
     * @param <T> entity type
     * @throws SQLException if generated key retrieval fails
     */
    private static <T> void assignGeneratedIds(
            List<T> objects,
            PreparedStatement statement)
            throws SQLException
    {
        Field primaryKey =
                ReflectionUtil.getPrimaryKeyField(
                        objects.get(0).getClass()
                );

        try (ResultSet generatedKeys =
                     statement.getGeneratedKeys())
        {
            int index = 0;

            while (generatedKeys.next()
                    && index < objects.size())
            {
                Object generatedId =
                        generatedKeys.getObject(1);

                ReflectionUtil.setFieldValue(
                        objects.get(index),
                        primaryKey,
                        generatedId
                );

                index++;
            }

            if (index != objects.size())
            {
                throw new MiniORMException(
                        "Database returned "
                                + index
                                + " generated key(s) for "
                                + objects.size()
                                + " inserted entities."
                );
            }
        }
    }

    /**
     * Maps the current ResultSet row to an entity object.
     *
     * @param clazz entity class
     * @param resultSet result set
     * @param <T> entity type
     * @return mapped entity
     * @throws Exception if reflection or mapping fails
     */
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
            String columnName =
                    ReflectionUtil.getColumnName(field);

            Object value =
                    resultSet.getObject(columnName);

            ReflectionUtil.setFieldValue(
                    object,
                    field,
                    value
            );
        }

        return object;
    }

    /**
     * Validates a single entity object.
     *
     * @param object entity to validate
     */
    private static void validateObject(
            Object object)
    {
        if (object == null)
        {
            throw new MiniORMException(
                    "Entity object cannot be null."
            );
        }
    }

    /**
     * Validates that all entities in a batch are non-null
     * and belong to the same class.
     *
     * @param objects entities to validate
     */
    private static void validateBatch(
            List<?> objects)
    {
        Class<?> entityClass =
                objects.get(0).getClass();

        for (Object object : objects)
        {
            if (object == null)
            {
                throw new MiniORMException(
                        "Batch cannot contain null entities."
                );
            }

            if (object.getClass() != entityClass)
            {
                throw new MiniORMException(
                        "All entities in a batch must have "
                                + "the same type. Expected "
                                + entityClass.getSimpleName()
                                + " but found "
                                + object.getClass().getSimpleName()
                                + "."
                );
            }
        }
    }
}