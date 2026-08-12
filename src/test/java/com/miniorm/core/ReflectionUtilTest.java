package com.miniorm.core;

import com.miniorm.entity.Product;
import com.miniorm.entity.User;
import com.miniorm.exception.MiniORMException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionUtilTest
{
    @Test
    void shouldGetTableName()
    {
        assertEquals(
                "users",
                ReflectionUtil.getTableName(User.class)
        );

        assertEquals(
                "products",
                ReflectionUtil.getTableName(Product.class)
        );
    }

    @Test
    void shouldGetColumnName()
            throws NoSuchFieldException
    {
        Field field =
                User.class.getDeclaredField("name");

        assertEquals(
                "name",
                ReflectionUtil.getColumnName(field)
        );
    }

    @Test
    void shouldFindPrimaryKey()
    {
        Field primaryKey =
                ReflectionUtil.getPrimaryKeyField(
                        User.class
                );

        assertEquals(
                "id",
                primaryKey.getName()
        );
    }

    @Test
    void shouldGetEntityFields()
    {
        Field[] fields =
                ReflectionUtil.getFields(
                        User.class
                );

        assertEquals(3, fields.length);
    }

    @Test
    void shouldValidateColumn()
    {
        String column =
                ReflectionUtil.validateColumn(
                        User.class,
                        "name"
                );

        assertEquals(
                "name",
                column
        );
    }

    @Test
    void shouldRejectInvalidColumn()
    {
        assertThrows(
                MiniORMException.class,
                () ->
                        ReflectionUtil.validateColumn(
                                User.class,
                                "password"
                        )
        );
    }

    @Test
    void shouldReturnValidColumnNames()
    {
        Set<String> columns =
                ReflectionUtil.getValidColumnNames(
                        User.class
                );

        assertTrue(columns.contains("id"));
        assertTrue(columns.contains("name"));
        assertTrue(columns.contains("email"));
    }
}