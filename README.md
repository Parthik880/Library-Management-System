# Library Management System

A Java Swing + PostgreSQL library-management desktop application for librarians.

## Current features

- Modern Swing UI with FlatLaf
- Sidebar navigation
- Members management
- Add, delete, and view member details
- Member issue status
- Books management
- Add, delete, and search books
- Book stock information
- View all students currently issued a particular book
- PostgreSQL database
- JDBC integration

## Architecture

```
Swing Frontend
      ↓
Section-specific database classes
      ↓
JDBC
      ↓
PostgreSQL
```

## Technology

- Java
- Swing
- FlatLaf
- PostgreSQL
- JDBC
- Maven

## Run

Use Java 17 and PostgreSQL. Create a database named `library_db`, then import the schema and sample data:

```sh
psql -U postgres -d library_db -f database/library.sql
```

Set `YOUR_POSTGRES_PASSWORD` in both database classes to the local PostgreSQL password before running. Do not commit that value.

```sh
mvn clean compile
mvn exec:java
```

The database rules allow one active issued book per student. A transaction with `submission_date IS NULL` is active; returned books remain in the transaction history. Status values are `Issued`, `Late`, and `Submitted`, and late fines are Rs. 5 per day.
