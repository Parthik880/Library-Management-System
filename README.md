# Library Management System

A small Java Swing, JDBC and PostgreSQL project for learning. The librarian application has five Java classes and three tabs: Books, Members, and Issue / Return. The separate student application has one file, `student/StudentView.java`.

## Run it

1. Create a PostgreSQL database named `library_db` and import `database/library.sql` once.
2. Set `LIBRARY_DB_URL`, `LIBRARY_DB_USER`, and `LIBRARY_DB_PASSWORD` if the default connection (`jdbc:postgresql://localhost:5432/library_db`, user `postgres`) does not match your computer.
3. From `librarian`, run `mvn clean compile` and then `mvn exec:java`.
4. From `student`, run `mvn clean compile` and then `mvn exec:java` to open the two-tab student view.

The librarian source is in `librarian/src/main/java/library`. See `STUDY_GUIDE.md` for a guided explanation.
