package com.miniorm;

import com.miniorm.config.DBConnection;
import com.miniorm.core.QueryOperator;
import com.miniorm.entity.Product;
import com.miniorm.entity.User;
import com.miniorm.repository.GenericRepository;

import java.util.List;

/**
 * Demonstrates the core features of the MiniORM framework.
 *
 * <p>This class demonstrates CRUD operations, batch processing,
 * dynamic queries, reflection-based mapping and connection pooling.
 *
 * @author Mayur Lakum
 */
public class Main
{
    public static void main(String[] args)
    {
        GenericRepository<User, Integer> userRepository =
                new GenericRepository<>(User.class);

        GenericRepository<Product, Integer> productRepository =
                new GenericRepository<>(Product.class);

        User user = null;
        Product product = null;
        List<Product> batchProducts = null;

        try
        {
            System.out.println();
            System.out.println("=================================================");
            System.out.println("              MiniORM Framework Demo");
            System.out.println("=================================================");

            // --------------------------------------------------
            // TEST 1 : SAVE
            // --------------------------------------------------

            System.out.println("\n[TEST 1] Save User");

            user = new User(
                    0,
                    "Yuvraj",
                    "yuvraj@gmail.com"
            );

            userRepository.save(user);

            System.out.println("Inserted: " + user);

            // --------------------------------------------------
            // TEST 2 : FIND BY ID
            // --------------------------------------------------

            System.out.println("\n[TEST 2] Find User By ID");

            User foundUser =
                    userRepository.findById(user.getId());

            System.out.println("Found: " + foundUser);

            // --------------------------------------------------
            // TEST 3 : UPDATE
            // --------------------------------------------------

            System.out.println("\n[TEST 3] Update User");

            foundUser.setName("Mayur Lakum");

            userRepository.update(foundUser);

            System.out.println(
                    "Updated: " +
                            userRepository.findById(foundUser.getId())
            );

            // --------------------------------------------------
            // TEST 4 : FIND ALL
            // --------------------------------------------------

            System.out.println("\n[TEST 4] Find All Users");

            List<User> users =
                    userRepository.findAll();

            System.out.println(
                    "Total users found: " + users.size()
            );

            // --------------------------------------------------
            // TEST 5 : FIND BY COLUMN
            // --------------------------------------------------

            System.out.println("\n[TEST 5] Find By Column");

            List<User> result =
                    userRepository.findByColumn(
                            "name",
                            "Mayur Lakum"
                    );

            result.forEach(System.out::println);

            // --------------------------------------------------
            // TEST 6 : SAVE PRODUCT
            // --------------------------------------------------

            System.out.println("\n[TEST 6] Save Product");

            product = new Product(
                    0,
                    "MiniORM Laptop",
                    55000
            );

            productRepository.save(product);

            System.out.println("Inserted: " + product);

            // --------------------------------------------------
            // TEST 7 : BATCH INSERT
            // --------------------------------------------------

            System.out.println("\n[TEST 7] Batch Insert");

            batchProducts = List.of(
                    new Product(0, "MiniORM Mouse", 500),
                    new Product(0, "MiniORM Keyboard", 1200),
                    new Product(0, "MiniORM Monitor", 9000)
            );

            productRepository.saveAll(batchProducts);

            System.out.println(
                    "Batch inserted: " +
                            batchProducts.size() +
                            " products"
            );

            // --------------------------------------------------
            // TEST 8 : DYNAMIC QUERY
            // --------------------------------------------------

            System.out.println("\n[TEST 8] Dynamic Query");

            List<Product> expensiveProducts =
                    productRepository.findWhere(
                            "price",
                            QueryOperator.GREATER_THAN,
                            50000
                    );

            System.out.println(
                    "Products with price > 50000: " +
                            expensiveProducts.size()
            );

            expensiveProducts.forEach(
                    System.out::println
            );

            // --------------------------------------------------
            // TEST 9 : FIND PRODUCT BY ID
            // --------------------------------------------------

            System.out.println("\n[TEST 9] Find Product By ID");

            Product foundProduct =
                    productRepository.findById(
                            product.getId()
                    );

            System.out.println("Found: " + foundProduct);

            // --------------------------------------------------
            // TEST 10 : DELETE USER
            // --------------------------------------------------

            System.out.println("\n[TEST 10] Delete User");

            userRepository.delete(user.getId());

            System.out.println(
                    "User deleted successfully."
            );

            System.out.println();
            System.out.println("=================================================");
            System.out.println("      ALL MINIORM FEATURES EXECUTED SUCCESSFULLY");
            System.out.println("=================================================");
        }
        finally
        {
            /*
             * Clean up demo data so repeated executions
             * do not continuously pollute the database.
             */

            if (batchProducts != null)
            {
                for (Product batchProduct : batchProducts)
                {
                    if (batchProduct.getId() > 0)
                    {
                        productRepository.delete(
                                batchProduct.getId()
                        );
                    }
                }
            }

            if (product != null && product.getId() > 0)
            {
                productRepository.delete(product.getId());
            }

            DBConnection.shutdown();
        }
    }
}