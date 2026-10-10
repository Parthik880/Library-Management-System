# Study Guide: Library Management System

## The five classes

| Class | Job |
| --- | --- |
| `Main` | Starts Swing, creates the window and its `JTabbedPane`, and contains small shared helpers. |
| `BooksPanel` | Displays the book table and handles add, search, view-all, and delete buttons. |
| `MembersPanel` | Displays the member table and handles add, search, view-all, and delete buttons. |
| `IssueReturnPanel` | Lets a librarian view a member's active loan, issue a book, or return it. |
| `Database` | Opens JDBC connections and contains every SQL query. |

There is deliberately no dashboard, sidebar, history screen, custom dialog class, or extra service layer. A row in a `JTable` is the simple “view” of a book or member.

## Student view

`student/src/main/java/student/StudentView.java` is intentionally separate from the five librarian classes. It asks for a roll number, displays exactly two tabs, and has its own small JDBC code using the same connection variables. Its issued-books query includes both `t.roll_no = ?` and `t.submission_date IS NULL`, so it shows only the logged-in student's active loans.

## Start-up flow

`Main.main` asks Swing to call `Main.open`. `open` creates a `JFrame`, adds a `JTabbedPane`, and puts one panel on each tab. When `BooksPanel` and `MembersPanel` are constructed, each calls its `load` method. That method asks `Database` for rows and puts them in the table model.

## Button click to SQL query

For example, clicking **Add Book** runs this flow:

1. The button listener in `BooksPanel` calls `addBook`.
2. `addBook` reads text from the six `JTextField` components and checks that Copies is a number.
3. It calls `Database.addBook`.
4. `Database.connect` reads the database connection environment variables and opens a JDBC `Connection`.
5. `Database.addBook` creates a `PreparedStatement` for an `INSERT INTO books ... VALUES (?, ?, ...)` query, fills each `?` with `setString` or `setInt`, then calls `executeUpdate`.
6. The panel clears its fields and calls `load`, so the `JTable` shows the saved row. If JDBC throws an error, `Main.error` shows it in a `JOptionPane`.

Search works the same way, except it sends `%search text%` values to a `SELECT` query using `ILIKE`. Delete reads the selected table row's ID and sends it to a prepared `DELETE` query. PostgreSQL may reject deleting a book or member with transaction history; the error is shown instead of hiding the integrity problem.

## Issuing a book

Clicking **Issue Book** calls `Database.issueBook`. This is a JDBC transaction:

1. `setAutoCommit(false)` starts work that will not be saved until `commit`.
2. The member row is selected with `FOR UPDATE`. This confirms that the member exists and prevents two simultaneous requests from giving that member two active books.
3. The method checks for an existing active transaction.
4. The book row is selected with `FOR UPDATE`, then its available copies are checked.
5. It inserts a transaction row with a due date 14 days from today and reduces `available_copies` by one.
6. `commit` saves both changes together. If any query fails, `rollback` undoes every change from this attempt.

That all-or-nothing behavior means a failed stock update cannot leave a loan record without reducing stock.

## Returning a book

**Return Book** calls `Database.returnBook`, which also uses `setAutoCommit(false)`. It finds the active transaction, calculates the late fine as days late × 5, changes the transaction to `Submitted`, and increases `available_copies`. Both updates are committed together, or both are rolled back if one fails.

## Database tables

The existing `members`, `books`, and `transactions` tables are kept. `books.total_copies` is the physical count; `books.available_copies` changes on issue and return. A transaction with `submission_date IS NULL` is an active issue. The existing schema ensures IDs are unique, values are valid, and transactions refer only to real books and members.

## Configuration and safety

The default connection is `jdbc:postgresql://localhost:5432/library_db` as user `postgres`. Use `LIBRARY_DB_URL`, `LIBRARY_DB_USER`, and `LIBRARY_DB_PASSWORD` to override it; do not place a password in source code. All runtime values use `PreparedStatement` parameters rather than building SQL from typed text.
