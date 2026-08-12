package com.miniorm.core;

import com.miniorm.annotations.Column;
import com.miniorm.annotations.Entity;
import com.miniorm.annotations.Id;
import com.miniorm.annotations.Table;
import com.miniorm.exception.MiniORMException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MiniORMExceptionTest
{
    // =========================================================
    // NULL CLASS
    // =========================================================

    @Test
    void shouldRejectNullEntityClass()
    {
        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.getTableName(null)
        );
    }

    // =========================================================
    // MISSING @ENTITY
    // =========================================================

    @Test
    void shouldRejectClassWithoutEntityAnnotation()
    {
        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.getTableName(
                        MissingEntity.class
                )
        );
    }

    // =========================================================
    // MISSING @TABLE
    // =========================================================

    @Test
    void shouldRejectEntityWithoutTableAnnotation()
    {
        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.getTableName(
                        MissingTable.class
                )
        );
    }

    // =========================================================
    // EMPTY TABLE NAME
    // =========================================================

    @Test
    void shouldRejectEmptyTableName()
    {
        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.getTableName(
                        EmptyTableName.class
                )
        );
    }

    // =========================================================
    // INVALID COLUMN
    // =========================================================

    @Test
    void shouldRejectInvalidColumn()
    {
        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.validateColumn(
                        ValidEntity.class,
                        "invalid_column"
                )
        );
    }

    // =========================================================
    // NULL COLUMN
    // =========================================================

    @Test
    void shouldRejectNullColumn()
    {
        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.validateColumn(
                        ValidEntity.class,
                        null
                )
        );
    }

    // =========================================================
    // BLANK COLUMN
    // =========================================================

    @Test
    void shouldRejectBlankColumn()
    {
        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.validateColumn(
                        ValidEntity.class,
                        "   "
                )
        );
    }

    // =========================================================
    // NULL FIELD
    // =========================================================

    @Test
    void shouldRejectNullField()
    {
        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.getColumnName(null)
        );
    }

    // =========================================================
    // FIELD WITHOUT @COLUMN
    // =========================================================

    @Test
    void shouldRejectFieldWithoutColumnAnnotation()
            throws NoSuchFieldException
    {
        Field field =
                FieldWithoutColumn.class.getDeclaredField("name");

        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.getColumnName(field)
        );
    }

    // =========================================================
    // NULL ENTITY OBJECT
    // =========================================================

    @Test
    void shouldRejectNullEntityObject()
    {
        Field field =
                getField(
                        ValidEntity.class,
                        "name"
                );

        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.getFieldValue(
                        null,
                        field
                )
        );
    }

    // =========================================================
    // NULL FIELD OBJECT
    // =========================================================

    @Test
    void shouldRejectNullFieldObject()
    {
        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.getFieldValue(
                        new ValidEntity(),
                        null
                )
        );
    }

    // =========================================================
    // MISSING PRIMARY KEY
    // =========================================================

    @Test
    void shouldRejectEntityWithoutPrimaryKey()
    {
        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.getPrimaryKeyField(
                        NoPrimaryKey.class
                )
        );
    }

    // =========================================================
    // MULTIPLE PRIMARY KEYS
    // =========================================================

    @Test
    void shouldRejectMultiplePrimaryKeys()
    {
        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.getPrimaryKeyField(
                        MultiplePrimaryKeys.class
                )
        );
    }

    // =========================================================
    // NULL QUERY OPERATOR
    // =========================================================

    @Test
    void shouldRejectNullQueryOperator()
    {
        assertThrows(
                MiniORMException.class,
                () -> SQLGenerator.generateFindByColumnQuery(
                        ValidEntity.class,
                        "name",
                        null
                )
        );
    }

    // =========================================================
    // NULL ENTITY FOR INSERT VALUES
    // =========================================================

    @Test
    void shouldRejectNullEntityForInsertValues()
    {
        assertThrows(
                MiniORMException.class,
                () -> SQLGenerator.getInsertValues(null)
        );
    }

    // =========================================================
    // NULL ENTITY FOR INSERT QUERY
    // =========================================================

    @Test
    void shouldRejectNullClassForInsertQuery()
    {
        assertThrows(
                MiniORMException.class,
                () -> SQLGenerator.generateInsertQuery(null)
        );
    }

    // =========================================================
    // NULL ENTITY FOR UPDATE QUERY
    // =========================================================

    @Test
    void shouldRejectNullClassForUpdateQuery()
    {
        assertThrows(
                MiniORMException.class,
                () -> SQLGenerator.generateUpdateQuery(null)
        );
    }

    // =========================================================
    // NULL ENTITY FOR DELETE QUERY
    // =========================================================

    @Test
    void shouldRejectNullClassForDeleteQuery()
    {
        assertThrows(
                MiniORMException.class,
                () -> SQLGenerator.generateDeleteQuery(null)
        );
    }

    // =========================================================
    // NULL ENTITY FOR FIND ALL
    // =========================================================

    @Test
    void shouldRejectNullClassForFindAllQuery()
    {
        assertThrows(
                MiniORMException.class,
                () -> SQLGenerator.generateFindAllQuery(null)
        );
    }

    // =========================================================
    // NULL VALUE INTO PRIMITIVE
    // =========================================================

    @Test
    void shouldRejectNullForPrimitiveField()
            throws NoSuchFieldException
    {
        Field field =
                PrimitiveEntity.class.getDeclaredField("id");

        assertThrows(
                MiniORMException.class,
                () -> ReflectionUtil.setFieldValue(
                        new PrimitiveEntity(),
                        field,
                        null
                )
        );
    }

    // =========================================================
    // HELPER
    // =========================================================

    private static Field getField(
            Class<?> clazz,
            String fieldName)
    {
        try
        {
            return clazz.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    // =========================================================
    // TEST ENTITIES
    // =========================================================

    @Entity
    @Table(name = "valid_entities")
    static class ValidEntity
    {
        @Id
        @Column(name = "id")
        private int id;

        @Column(name = "name")
        private String name;
    }

    // No @Entity
    @Table(name = "missing_entity")
    static class MissingEntity
    {
    }

    // @Entity but no @Table
    @Entity
    static class MissingTable
    {
    }

    @Entity
    @Table(name = "")
    static class EmptyTableName
    {
    }

    @Entity
    @Table(name = "no_primary_key")
    static class NoPrimaryKey
    {
        @Column(name = "name")
        private String name;
    }

    @Entity
    @Table(name = "multiple_primary_keys")
    static class MultiplePrimaryKeys
    {
        @Id
        @Column(name = "id1")
        private int id1;

        @Id
        @Column(name = "id2")
        private int id2;
    }

    static class FieldWithoutColumn
    {
        private String name;
    }

    @Entity
    @Table(name = "primitive_entities")
    static class PrimitiveEntity
    {
        @Id
        @Column(name = "id")
        private int id;
    }
}