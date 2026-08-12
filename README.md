# MiniORM

A lightweight ORM framework built from scratch in Java using **Reflection, Custom Annotations, JDBC, SQL Generation, HikariCP Connection Pooling, and a Generic Repository Pattern**.

MiniORM maps Java objects to relational database tables and provides a simple repository-based API for performing database operations without writing repetitive JDBC code.

---

## 🚀 Features

- Custom ORM annotations
    - `@Entity`
    - `@Table`
    - `@Column`
    - `@Id`

- Reflection-based entity mapping
- Automatic table and column resolution
- Primary-key detection
- Reflection metadata caching
- Dynamic SQL generation
- CRUD operations
    - Save
    - Find By ID
    - Find All
    - Update
    - Delete
- Find by column
- Dynamic WHERE queries
- Type-safe query operators
- Batch insert operations
- Generic Repository pattern
- JDBC database communication
- HikariCP connection pooling
- Centralized custom exception handling
- Database configuration using properties/environment variables
- Unit testing
- Database integration testing
- Edge-case and exception testing

---

## 🛠️ Technology Stack

| Technology | Purpose |
|------------|---------|
| Java 23 | Core framework implementation |
| JDBC | Database communication |
| MySQL | Relational database |
| HikariCP | Database connection pooling |
| Maven | Dependency management and build |
| JUnit 5 | Testing |
| Reflection API | Runtime entity mapping |
| Custom Annotations | ORM metadata |
| SLF4J | Logging |

---

## 🏗️ Architecture

MiniORM follows a layered architecture:

```text
Application
     │
     ▼
GenericRepository
     │
     ▼
EntityManager
     │
     ├──────────────► ReflectionUtil
     │                    │
     │                    ▼
     │              Entity Metadata
     │
     ├──────────────► SQLGenerator
     │                    │
     │                    ▼
     │               SQL Queries
     │
     ▼
JDBC
     │
     ▼
HikariCP
     │
     ▼
MySQL