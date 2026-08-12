package com.miniorm.repository;

import com.miniorm.config.DBConnection;
import com.miniorm.core.QueryOperator;
import com.miniorm.entity.Product;
import com.miniorm.entity.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenericRepositoryIntegrationTest
{
    private static GenericRepository<User, Integer> userRepository;
    private static GenericRepository<Product, Integer> productRepository;

    @BeforeAll
    static void setUp()
    {
        userRepository =
                new GenericRepository<>(User.class);

        productRepository =
                new GenericRepository<>(Product.class);
    }

    @Test
    void shouldSaveAndFindUser()
    {
        User user =
                new User(
                        0,
                        "MiniORM Test User",
                        "miniorm.test@gmail.com"
                );

        userRepository.save(user);

        try
        {
            assertTrue(user.getId() > 0);

            User found =
                    userRepository.findById(
                            user.getId()
                    );

            assertNotNull(found);

            assertEquals(
                    user.getId(),
                    found.getId()
            );

            assertEquals(
                    "MiniORM Test User",
                    found.getName()
            );

            assertEquals(
                    "miniorm.test@gmail.com",
                    found.getEmail()
            );
        }
        finally
        {
            if (user.getId() > 0)
            {
                userRepository.delete(
                        user.getId()
                );
            }
        }
    }

    @Test
    void shouldFindAllUsers()
    {
        User user =
                new User(
                        0,
                        "MiniORM Find All Test",
                        "findall@miniorm.test"
                );

        userRepository.save(user);

        try
        {
            List<User> users =
                    userRepository.findAll();

            assertNotNull(users);

            assertFalse(users.isEmpty());

            assertTrue(
                    users.stream()
                            .anyMatch(
                                    u -> u.getId()
                                            == user.getId()
                            )
            );
        }
        finally
        {
            if (user.getId() > 0)
            {
                userRepository.delete(
                        user.getId()
                );
            }
        }
    }

    @Test
    void shouldFindUserByColumn()
    {
        User user =
                new User(
                        0,
                        "MiniORM Column Test",
                        "column@miniorm.test"
                );

        userRepository.save(user);

        try
        {
            List<User> users =
                    userRepository.findByColumn(
                            "email",
                            "column@miniorm.test"
                    );

            assertFalse(users.isEmpty());

            assertTrue(
                    users.stream()
                            .anyMatch(
                                    u -> u.getId()
                                            == user.getId()
                            )
            );
        }
        finally
        {
            if (user.getId() > 0)
            {
                userRepository.delete(
                        user.getId()
                );
            }
        }
    }

    @Test
    void shouldUpdateUser()
    {
        User user =
                new User(
                        0,
                        "MiniORM Before Update",
                        "update@miniorm.test"
                );

        userRepository.save(user);

        try
        {
            assertTrue(user.getId() > 0);

            user.setName(
                    "MiniORM After Update"
            );

            userRepository.update(user);

            User updated =
                    userRepository.findById(
                            user.getId()
                    );

            assertNotNull(updated);

            assertEquals(
                    "MiniORM After Update",
                    updated.getName()
            );

            assertEquals(
                    "update@miniorm.test",
                    updated.getEmail()
            );
        }
        finally
        {
            if (user.getId() > 0)
            {
                userRepository.delete(
                        user.getId()
                );
            }
        }
    }

    @Test
    void shouldSaveProduct()
    {
        Product product =
                new Product(
                        0,
                        "MiniORM Test Product",
                        75000
                );

        productRepository.save(product);

        try
        {
            assertTrue(product.getId() > 0);

            Product found =
                    productRepository.findById(
                            product.getId()
                    );

            assertNotNull(found);

            assertEquals(
                    "MiniORM Test Product",
                    found.getName()
            );

            assertEquals(
                    75000,
                    found.getPrice()
            );
        }
        finally
        {
            if (product.getId() > 0)
            {
                productRepository.delete(
                        product.getId()
                );
            }
        }
    }

    @Test
    void shouldFindProductsUsingDynamicQuery()
    {
        Product product =
                new Product(
                        0,
                        "MiniORM Dynamic Query Product",
                        75000
                );

        productRepository.save(product);

        try
        {
            List<Product> products =
                    productRepository.findWhere(
                            "price",
                            QueryOperator.GREATER_THAN,
                            70000
                    );

            assertNotNull(products);

            assertTrue(
                    products.stream()
                            .anyMatch(
                                    p -> p.getId()
                                            == product.getId()
                            )
            );

            assertTrue(
                    products.stream()
                            .allMatch(
                                    p -> p.getPrice()
                                            > 70000
                            )
            );
        }
        finally
        {
            if (product.getId() > 0)
            {
                productRepository.delete(
                        product.getId()
                );
            }
        }
    }

    @Test
    void shouldSaveProductsInBatch()
    {
        List<Product> products =
                List.of(
                        new Product(
                                0,
                                "MiniORM Batch Mouse",
                                500
                        ),
                        new Product(
                                0,
                                "MiniORM Batch Keyboard",
                                1200
                        ),
                        new Product(
                                0,
                                "MiniORM Batch Monitor",
                                9000
                        )
                );

        productRepository.saveAll(products);

        try
        {
            assertEquals(
                    3,
                    products.size()
            );

            for (Product product : products)
            {
                assertTrue(
                        product.getId() > 0
                );
            }
        }
        finally
        {
            for (Product product : products)
            {
                if (product.getId() > 0)
                {
                    productRepository.delete(
                            product.getId()
                    );
                }
            }
        }
    }

    @Test
    void shouldDeleteUser()
    {
        User user =
                new User(
                        0,
                        "MiniORM Delete User",
                        "delete@miniorm.test"
                );

        userRepository.save(user);

        assertTrue(user.getId() > 0);

        int id = user.getId();

        userRepository.delete(id);

        User deleted =
                userRepository.findById(id);

        assertNull(deleted);
    }

    @Test
    void shouldDeleteProduct()
    {
        Product product =
                new Product(
                        0,
                        "MiniORM Delete Product",
                        5000
                );

        productRepository.save(product);

        assertTrue(product.getId() > 0);

        int id = product.getId();

        productRepository.delete(id);

        Product deleted =
                productRepository.findById(id);

        assertNull(deleted);
    }

    @AfterAll
    static void tearDown()
    {
        DBConnection.shutdown();
    }
}