package com.miniorm.core;

import com.miniorm.entity.Product;
import com.miniorm.entity.User;
import com.miniorm.exception.MiniORMException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SQLGeneratorTest
{
    @Test
    void shouldGenerateInsertQuery()
    {
        String query =
                SQLGenerator.generateInsertQuery(
                        User.class
                );

        assertEquals(
                "INSERT INTO users (name,email) VALUES (?,?)",
                query
        );
    }

    @Test
    void shouldGenerateProductInsertQuery()
    {
        String query =
                SQLGenerator.generateInsertQuery(
                        Product.class
                );

        assertEquals(
                "INSERT INTO products (name,price) VALUES (?,?)",
                query
        );
    }

    @Test
    void shouldGenerateUpdateQuery()
    {
        String query =
                SQLGenerator.generateUpdateQuery(
                        User.class
                );

        assertEquals(
                "UPDATE users SET name=?,email=? WHERE id=?",
                query
        );
    }

    @Test
    void shouldGenerateDeleteQuery()
    {
        String query =
                SQLGenerator.generateDeleteQuery(
                        User.class
                );

        assertEquals(
                "DELETE FROM users WHERE id = ?",
                query
        );
    }

    @Test
    void shouldGenerateFindAllQuery()
    {
        String query =
                SQLGenerator.generateFindAllQuery(
                        Product.class
                );

        assertEquals(
                "SELECT * FROM products",
                query
        );
    }

    @Test
    void shouldGenerateFindByColumnQuery()
    {
        String query =
                SQLGenerator.generateFindByColumnQuery(
                        User.class,
                        "name"
                );

        assertEquals(
                "SELECT * FROM users WHERE name = ?",
                query
        );
    }

    @Test
    void shouldGenerateDynamicQuery()
    {
        String query =
                SQLGenerator.generateFindByColumnQuery(
                        Product.class,
                        "price",
                        QueryOperator.GREATER_THAN
                );

        assertEquals(
                "SELECT * FROM products WHERE price > ?",
                query
        );
    }

    @Test
    void shouldGenerateGreaterOrEqualQuery()
    {
        String query =
                SQLGenerator.generateFindByColumnQuery(
                        Product.class,
                        "price",
                        QueryOperator.GREATER_OR_EQUAL
                );

        assertEquals(
                "SELECT * FROM products WHERE price >= ?",
                query
        );
    }

    @Test
    void shouldGenerateLikeQuery()
    {
        String query =
                SQLGenerator.generateFindByColumnQuery(
                        User.class,
                        "name",
                        QueryOperator.LIKE
                );

        assertEquals(
                "SELECT * FROM users WHERE name LIKE ?",
                query
        );
    }

    @Test
    void shouldRejectNullOperator()
    {
        assertThrows(
                MiniORMException.class,
                () ->
                        SQLGenerator.generateFindByColumnQuery(
                                User.class,
                                "name",
                                null
                        )
        );
    }

    @Test
    void shouldRejectInvalidColumn()
    {
        assertThrows(
                MiniORMException.class,
                () ->
                        SQLGenerator.generateFindByColumnQuery(
                                User.class,
                                "password",
                                QueryOperator.EQUALS
                        )
        );
    }

    @Test
    void shouldRejectNullClass()
    {
        assertThrows(
                MiniORMException.class,
                () ->
                        SQLGenerator.generateFindAllQuery(
                                null
                        )
        );
    }
}