# MiniORM

### A Lightweight ORM Framework Built from Scratch in Java

MiniORM is a lightweight **Object-Relational Mapping (ORM) framework** developed from scratch using **Java Reflection, Custom Annotations, JDBC, Dynamic SQL Generation, HikariCP, and the Generic Repository Pattern**.

The goal of this project was to understand how frameworks such as **Hibernate/JPA work internally** rather than simply using an existing ORM library.

MiniORM allows Java objects to be mapped to relational database tables and provides a repository-based API for performing database operations without writing repetitive JDBC code.

---

## 🎯 Why I Built MiniORM

Instead of directly using an ORM framework such as Hibernate, I built a simplified ORM from scratch to understand the underlying concepts:

- How Java classes can be mapped to database tables
- How annotations can store ORM metadata
- How Reflection can inspect and manipulate entity fields at runtime
- How SQL can be generated dynamically
- How JDBC executes generated SQL
- How database connections can be managed efficiently using a connection pool
- How generic repositories can provide reusable CRUD operations
- How frameworks validate and handle invalid metadata
- How reflection metadata can be cached for better performance

This project focuses on **framework internals and backend engineering concepts**, rather than building a normal CRUD application.

---

# 🏗️ How MiniORM Works

A developer defines a Java entity using MiniORM annotations:

```java
@Entity
@Table(name = "users")
public class User
{
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;
}
```

Instead of writing JDBC boilerplate by hand:

```java
PreparedStatement statement =
        connection.prepareStatement(
                "SELECT * FROM users WHERE id = ?"
        );

statement.setInt(1, id);

ResultSet resultSet = statement.executeQuery();
```

the application interacts with MiniORM through a generic repository:

```java
GenericRepository<User, Integer> repository =
        new GenericRepository<>(User.class);

User user = repository.findById(1);
```

Behind this single call, MiniORM determines the table, columns and primary key through annotations and Reflection, generates the required SQL, executes it through JDBC, and maps the result back into a Java object.

---

# Architecture

```text
┌───────────────────────────────┐
│         Application           │
│                               │
│ GenericRepository<User, ID>   │
└───────────────┬───────────────┘
                │
                ▼
┌───────────────────────────────┐
│        GenericRepository      │
│                               │
│ save / find / update / delete │
└───────────────┬───────────────┘
                │
                ▼
┌───────────────────────────────┐
│         EntityManager         │
│                               │
│ Coordinates ORM operations    │
└───────┬───────────────┬───────┘
        │               │
        ▼               ▼
┌──────────────┐  ┌──────────────┐
│ReflectionUtil│  │ SQLGenerator │
│              │  │              │
│Entity        │  │ INSERT       │
│metadata      │  │ UPDATE       │
│field mapping │  │ DELETE       │
│PK detection  │  │ SELECT       │
└──────┬───────┘  └──────┬───────┘
       │                 │
       └────────┬────────┘
                ▼
        ┌──────────────┐
        │     JDBC     │
        └──────┬───────┘
               ▼
        ┌──────────────┐
        │   HikariCP   │
        │ Connection   │
        │    Pool      │
        └──────┬───────┘
               ▼
        ┌──────────────┐
        │    MySQL     │
        └──────────────┘
```

Each layer has a single responsibility: repositories expose the public API, the entity manager coordinates a request, reflection resolves metadata, SQL generation builds parameterized statements, and JDBC/HikariCP handle execution against MySQL.

---

# Request Lifecycle

For example:

```java
repository.findWhere(
        "price",
        QueryOperator.GREATER_THAN,
        50000
);
```

MiniORM processes the request through the following pipeline:

```text
Application
     │
     ▼
GenericRepository
     │
     ▼
EntityManager
     │
     ├── Validate entity metadata
     │
     ├── Validate column name
     │
     └── Validate QueryOperator
     │
     ▼
SQLGenerator
     │
     ▼
SELECT * FROM products
WHERE price > ?
     │
     ▼
JDBC PreparedStatement
     │
     ▼
HikariCP Connection
     │
     ▼
MySQL
     │
     ▼
ResultSet
     │
     ▼
Reflection-based mapping
     │
     ▼
List<Product>
```

This separation keeps database execution, metadata processing, SQL generation and repository APIs independent of one another.

---

# Entity Mapping

MiniORM uses runtime annotations to describe how a Java class corresponds to a database table.

```java
@Entity
@Table(name = "products")
public class Product
{
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private double price;
}
```

| Annotation | Responsibility                               |
| ---------- | --------------------------------------------- |
| `@Entity`  | Marks a class as a persistent MiniORM entity  |
| `@Table`   | Defines the database table                    |
| `@Column`  | Maps a field to a database column             |
| `@Id`      | Identifies the entity primary key             |

The annotations use `@Retention(RetentionPolicy.RUNTIME)` so the metadata remains available when MiniORM processes entities at runtime.

---

# Reflection Engine

`ReflectionUtil` is one of the core components of MiniORM. It discovers and manipulates entity metadata dynamically:

```text
Class
 ├── @Entity
 ├── @Table → table name
 │
 └── Fields
      ├── @Id → primary key
      ├── @Column → database column
      └── Java field type
```

This allows the framework to work with any entity class without hardcoding `User`, `Product`, or any other application-specific type. For example:

```java
ReflectionUtil.getPrimaryKeyField(Product.class);
```

locates the field marked with `@Id`.

## Reflection Metadata Caching

Reflection metadata is cached using `ConcurrentHashMap`:

```text
Class<?> → Field[]
Class<?> → Primary Key Field
Class<?> → Valid Column Names
```

This prevents repeated reflection scans for the same entity class, since repository operations repeatedly need the entity's fields, primary key, table name and column names.

---

# SQL Generation

SQL generation is separated from database execution. `SQLGenerator` builds parameterized SQL based on entity metadata.

**Insert**
```sql
INSERT INTO products (name, price)
VALUES (?, ?)
```

**Update**
```sql
UPDATE products
SET name = ?, price = ?
WHERE id = ?
```

**Delete**
```sql
DELETE FROM products
WHERE id = ?
```

**Find All**
```sql
SELECT * FROM products
```

**Dynamic Query**
```sql
SELECT * FROM products
WHERE price > ?
```

The framework never concatenates entity values directly into SQL statements — values are always passed separately using JDBC parameters.

---

# Query Abstraction

Dynamic queries are represented using the `QueryOperator` enum:

```java
public enum QueryOperator
{
    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    GREATER_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_OR_EQUAL("<="),
    LIKE("LIKE");
}
```

This keeps the repository API expressive:

```java
productRepository.findWhere(
        "price",
        QueryOperator.GREATER_THAN,
        50000
);
```

instead of relying on raw operator strings. The column name is also validated against the entity's mapped columns before SQL generation, preventing arbitrary column names or operators from being injected into dynamically generated SQL.

---

# Generic Repository

MiniORM provides a generic repository abstraction, `GenericRepository<T, ID>`, which implements `CrudRepository<T, ID>` and exposes:

```java
save()
saveAll()
findById()
findAll()
findByColumn()
findWhere()
update()
delete()
```

The same repository implementation can operate on different entities — `GenericRepository<User, Integer>` or `GenericRepository<Product, Integer>` — simply by supplying the entity class once:

```java
new GenericRepository<>(Product.class);
```

MiniORM uses that type information to resolve the corresponding database metadata.

---

# Database Connection Management

MiniORM uses **HikariCP** instead of opening a new database connection for every operation:

```text
Application
     │
     ▼
DBConnection
     │
     ▼
HikariDataSource
     │
     ├── Connection
     ├── Connection
     ├── Connection
     └── Connection
             │
             ▼
            MySQL
```

Pool sizing is externalized:

```properties
db.pool.maxSize=10
db.pool.minIdle=2
```

The pool is initialized on first use and explicitly shut down when the application finishes.

---

# Configuration

Database configuration is separated from the framework implementation. MiniORM supports both environment variables and a `db.properties` file, with environment variables taking precedence.

Supported configuration:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
DB_POOL_MAX_SIZE
DB_POOL_MIN_IDLE
```

Example:

```properties
db.url=jdbc:mysql://localhost:3306/miniormdb
db.username=root
db.password=YOUR_DATABASE_PASSWORD

db.pool.maxSize=10
db.pool.minIdle=2
```

Actual credentials are excluded from version control; the repository ships a `db.properties.example` template instead.

---

# Exception Handling

Framework-specific failures are represented using `MiniORMException` instead of leaking low-level implementation details:

```text
Missing @Entity
Missing @Table
Missing @Id
Multiple @Id fields
Invalid column
Invalid configuration
Reflection failure
Database connection failure
```

```java
throw new MiniORMException(
        "Entity User does not contain an @Id field."
);
```

`MiniORMException` also supports exception chaining, preserving the original cause while adding framework-level context:

```java
new MiniORMException(
        "Failed to obtain database connection.",
        cause
);
```

---

# Batch Operations

MiniORM supports batch inserts through `saveAll()`:

```java
productRepository.saveAll(
        List.of(
                new Product(0, "Mouse", 500),
                new Product(0, "Keyboard", 1200),
                new Product(0, "Monitor", 9000)
        )
);
```

This shows the framework going beyond single-row CRUD to expose higher-level database functionality.

---

# Testing

**Unit Tests** cover individual components without requiring a database:

```text
ReflectionUtil
SQLGenerator
MiniORMException
```

**Integration Tests** (`GenericRepositoryIntegrationTest`) validate repository operations against a real MySQL database:

```text
Save
Find By ID
Find All
Update
Delete
Find By Column
Dynamic Queries
Batch Operations
```

The project is tested at both the component level and the database integration level.

---

# Demonstrated Workflow

```text
1. Create Entity
       ↓
2. Save
       ↓
3. Retrieve by ID
       ↓
4. Update
       ↓
5. Query
       ↓
6. Batch Insert
       ↓
7. Dynamic Query
       ↓
8. Delete
       ↓
9. Shutdown Connection Pool
```

```java
GenericRepository<Product, Integer> productRepository =
        new GenericRepository<>(Product.class);

Product product =
        new Product(0, "MiniORM Laptop", 55000);

productRepository.save(product);

Product result =
        productRepository.findById(product.getId());
```

---

# Project Structure

```text
MiniORM/
│
├── src/
│   ├── main/
│   │   ├── java/com/miniorm/
│   │   │
│   │   ├── annotations/
│   │   │   ├── Entity.java
│   │   │   ├── Table.java
│   │   │   ├── Column.java
│   │   │   └── Id.java
│   │   │
│   │   ├── config/
│   │   │   ├── DBConfig.java
│   │   │   └── DBConnection.java
│   │   │
│   │   ├── core/
│   │   │   ├── EntityManager.java
│   │   │   ├── ReflectionUtil.java
│   │   │   ├── SQLGenerator.java
│   │   │   └── QueryOperator.java
│   │   │
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   └── Product.java
│   │   │
│   │   ├── exception/
│   │   │   └── MiniORMException.java
│   │   │
│   │   ├── repository/
│   │   │   ├── CrudRepository.java
│   │   │   └── GenericRepository.java
│   │   │
│   │   └── Main.java
│   │
│   ├── main/resources/
│   │   └── db.properties.example
│   │
│   └── test/
│       └── java/com/miniorm/
│           ├── core/
│           │   ├── MiniORMExceptionTest.java
│           │   ├── ReflectionUtilTest.java
│           │   └── SQLGeneratorTest.java
│           │
│           └── repository/
│               └── GenericRepositoryIntegrationTest.java
│
├── pom.xml
├── .gitignore
└── README.md
```

---

# Technology Stack

| Technology             | Role                              |
| ----------------------- | ---------------------------------- |
| **Java 23**             | Framework implementation           |
| **JDBC**                | Low-level database communication   |
| **MySQL**               | Relational database                |
| **Java Reflection**     | Runtime entity inspection          |
| **Custom Annotations**  | ORM metadata                       |
| **HikariCP**            | Connection pooling                 |
| **Maven**               | Build and dependency management    |
| **JUnit 5**             | Automated testing                  |
| **SLF4J**               | Logging                            |
| **ConcurrentHashMap**   | Metadata caching                   |

---

# Engineering Concepts Demonstrated

- Object-oriented design
- Generic programming
- Java Reflection
- Runtime annotations
- Metadata-driven programming
- JDBC and prepared statements
- SQL generation
- Repository pattern
- Connection pooling
- Exception handling
- Configuration management
- Reflection caching
- Unit testing
- Integration testing
- Maven project structure
- Git/GitHub workflow

---

# Current Scope

MiniORM intentionally focuses on the core ORM pipeline:

```text
Java Entity → Annotations → Reflection Metadata → SQL Generation → JDBC → HikariCP → MySQL → Java Object
```

It is **not intended to be a replacement for production ORM frameworks such as Hibernate**. The purpose is to implement and understand the fundamental mechanisms behind ORM systems.

Potential future extensions include:

- Entity relationships
- Transactions
- Pagination and sorting
- Support for more database types
- Schema generation
- Advanced query construction
- Lazy loading
- First/second-level caching

---

# Running the Project

### Requirements
- Java 23
- Maven
- MySQL
- Git

### Clone
```bash
git clone https://github.com/Mayur-lakum/MiniORM.git
cd MiniORM
```

### Create Database
```sql
CREATE DATABASE miniormdb;
```

### Configure Database
Create `src/main/resources/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/miniormdb
db.username=root
db.password=YOUR_DATABASE_PASSWORD

db.pool.maxSize=10
db.pool.minIdle=2
```

### Run Tests
```bash
mvn clean test
```

### Run Demo
Run `com.miniorm.Main`.

---

# Project Status

| Component                  | Status |
| --------------------------- | ------ |
| Custom annotations          | ✅     |
| Entity mapping               | ✅     |
| Reflection engine            | ✅     |
| Reflection caching           | ✅     |
| Primary-key detection        | ✅     |
| SQL generation                | ✅     |
| CRUD operations               | ✅     |
| Dynamic queries                | ✅     |
| Query operators                 | ✅     |
| Batch insert                     | ✅     |
| Generic repository                | ✅     |
| JDBC integration                    | ✅     |
| HikariCP pooling                      | ✅     |
| Configuration management               | ✅     |
| Custom exception handling                | ✅     |
| Unit tests                                 | ✅     |
| Integration tests                            | ✅     |

---

# Author

**Mayur Lakum**
BE Information Technology
Java Backend Developer | Spring Boot | JDBC | SQL | Backend Systems

GitHub: [https://github.com/Mayur-lakum](https://github.com/Mayur-lakum)

---

## Key Takeaway

MiniORM is a **framework implementation project**, not a conventional CRUD application. It focuses on understanding what happens between a Java object and a database row:

```text
Java Object → ORM Metadata → Reflection → SQL Generation → JDBC → Database → Object Mapping
```

The framework encapsulates this pipeline behind a reusable repository API so that application code can work with Java entities rather than manually handling repetitive JDBC operations.