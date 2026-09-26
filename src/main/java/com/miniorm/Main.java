package com.miniorm;

import com.miniorm.config.DBConnection;
import com.miniorm.core.QueryOperator;
import com.miniorm.entity.Product;
import com.miniorm.entity.User;
import com.miniorm.repository.GenericRepository;

import java.util.List;

/**
 * MiniORM Framework Demo
 *
 * Uncomment only the test you want to run.
 *
 * Available tests:
 * 1. Save User
 * 2. Find User By ID
 * 3. Update User
 * 4. Find All Users
 * 5. Find User By Column
 * 6. Save Product
 * 7. Batch Insert
 * 8. Dynamic Query
 * 9. Find Product By ID
 * 10. Delete User
 */
public class Main {

    public static void main(String[] args) {

        GenericRepository<User, Integer> userRepository =
                new GenericRepository<>(User.class);

        GenericRepository<Product, Integer> productRepository =
                new GenericRepository<>(Product.class);

        try {

            System.out.println();
            System.out.println("=================================================");
            System.out.println("              MiniORM Framework Demo");
            System.out.println("=================================================");


            // =================================================
            // TEST 1 : SAVE USER
            // Uncomment this section to test INSERT operation.
            // =================================================

            /*
            System.out.println("\n[TEST 1] Save User");

            User user = new User(
                    0,
                    "Mayur",
                    "mayur@gmail.com"
            );

            userRepository.save(user);

            System.out.println("Inserted: " + user);
            */


            // =================================================
            // TEST 2 : FIND USER BY ID
            // Uncomment this section to test SELECT BY ID.
            // =================================================

            /*
            System.out.println("\n[TEST 2] Find User By ID");

            User user =
                    userRepository.findById(1);

            System.out.println("Found: " + user);
            */


            // =================================================
            // TEST 3 : UPDATE USER
            // Uncomment this section to test UPDATE operation.
            // =================================================

            /*
            System.out.println("\n[TEST 3] Update User");

            User user =
                    userRepository.findById(1);

            user.setName("Mayur Lakum");

            userRepository.update(user);

            System.out.println(
                    "Updated: " +
                            userRepository.findById(user.getId())
            );
            */


            // =================================================
            // TEST 4 : FIND ALL USERS
            // Uncomment this section to test SELECT ALL.
            // =================================================

            /*
            System.out.println("\n[TEST 4] Find All Users");

            List<User> users =
                    userRepository.findAll();

            System.out.println(
                    "Total users found: " +
                            users.size()
            );

            users.forEach(System.out::println);
            */


            // =================================================
            // TEST 5 : FIND USER BY COLUMN
            // Uncomment this section to test column-based search.
            // =================================================

            /*
            System.out.println("\n[TEST 5] Find By Column");

            List<User> users =
                    userRepository.findByColumn(
                            "name",
                            "Mayur Lakum"
                    );

            users.forEach(System.out::println);
            */


            // =================================================
            // TEST 6 : SAVE PRODUCT
            // Uncomment this section to test Product INSERT.
            // =================================================

            /*
            System.out.println("\n[TEST 6] Save Product");

            Product product = new Product(
                    0,
                    "MiniORM Laptop",
                    55000
            );

            productRepository.save(product);

            System.out.println(
                    "Inserted: " + product
            );
            */


            // =================================================
            // TEST 7 : BATCH INSERT
            // Uncomment this section to test batch INSERT.
            // =================================================

            /*
            System.out.println("\n[TEST 7] Batch Insert");

            List<Product> products = List.of(
                    new Product(0, "MiniORM Mouse", 500),
                    new Product(0, "MiniORM Keyboard", 1200),
                    new Product(0, "MiniORM Monitor", 9000)
            );

            productRepository.saveAll(products);

            System.out.println(
                    "Batch inserted: " +
                            products.size() +
                            " products"
            );
            */


            // =================================================
            // TEST 8 : DYNAMIC QUERY
            // Uncomment this section to test dynamic WHERE.
            // =================================================

            /*
            System.out.println("\n[TEST 8] Dynamic Query");

            List<Product> products =
                    productRepository.findWhere(
                            "price",
                            QueryOperator.GREATER_THAN,
                            50000
                    );

            System.out.println(
                    "Products with price > 50000: " +
                            products.size()
            );

            products.forEach(System.out::println);
            */


            // =================================================
            // TEST 9 : FIND PRODUCT BY ID
            // Uncomment this section to test Product SELECT BY ID.
            // =================================================

            /*
            System.out.println("\n[TEST 9] Find Product By ID");

            Product product =
                    productRepository.findById(1);

            System.out.println(
                    "Found: " + product
            );
            */


            // =================================================
            // TEST 10 : DELETE USER
            // Uncomment this section to test DELETE operation.
            // =================================================

            /*
            System.out.println("\n[TEST 10] Delete User");

            userRepository.delete(1);

            System.out.println(
                    "User deleted successfully."
            );
            */


            System.out.println();
            System.out.println("=================================================");
            System.out.println("           MINIORM DEMO COMPLETED");
            System.out.println("=================================================");

        } finally {

            // Close the database connection pool.
            DBConnection.shutdown();
        }
    }
}