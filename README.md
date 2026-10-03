# Library Management System

Two independent Java Swing desktop applications using one PostgreSQL database:
a librarian application and a read-only student portal. Both use Java 17,
FlatLaf, JDBC and Maven.

## Project structure

```text
library-system/
├── librarian/
│   ├── pom.xml
│   └── src/main/java/
│       ├── frontend/
│       │   ├── Main.java
│       │   ├── DashboardPanel.java
│       │   ├── MembersPanel.java
│       │   ├── BooksPanel.java
│       │   ├── IssueReturnPanel.java
│       │   └── HistoryPanel.java
│       └── backend/
│           ├── Database.java
│           ├── MembersDatabase.java
│           ├── BooksDatabase.java
│           └── TransactionDatabase.java
├── student/
│   ├── pom.xml
│   └── src/main/java/
│       ├── frontend/
│       │   ├── StudentMain.java
│       │   ├── StudentBooksPanel.java
│       │   ├── MyBooksPanel.java
│       │   └── FinePanel.java
│       └── backend/
│           └── StudentDatabase.java
├── database/library.sql
├── graphify/
└── tests/
    ├── LibraryCheck.java
    ├── UiCheck.java
    └── check.ps1
```

Each app has its own source tree, dependencies and entry point. The student
app does not import or depend on librarian code.

## Features

The librarian uses sidebar navigation and one JFrame with CardLayout:

- Dashboard: member/title counts, available/issued copies, overdue issues and fines.
- Members: search, add, delete and scrollable details with the student's active issue.
- Books: search, add, delete, stock details, current borrowers, and Change Book Settings.
- Issue / Return: issue for 14 days or return a matching active loan.
- Transaction History: search both active and submitted transactions.

The student portal asks for an existing roll number, then offers:

- Books: search the full catalogue and view personal issue status and live fine.
- My Books: current and returned loans, with scrollable transaction details.
- Fines: current overdue fines plus recorded fines from returned books.

Roll-number entry is identification for this mini-project, not secure authentication.
Student JDBC connections are read-only and its backend contains only SELECT queries.
For a shared deployment, use a PostgreSQL account with SELECT-only privileges for students.

## Database and connection

Both apps default to `jdbc:postgresql://localhost:5432/library_db` and user
`postgres`. They use the same environment variables:

| Variable | Purpose |
| --- | --- |
| `LIBRARY_DB_URL` | Optional JDBC URL override |
| `LIBRARY_DB_USER` | Optional PostgreSQL user override |
| `LIBRARY_DB_PASSWORD` | PostgreSQL password, supplied outside Git |

Set these in the terminal that runs Maven. No password is stored in Java source
and neither app loads a `.env` file automatically. Do not commit credentials.

Reuse an existing database containing `members`, `books` and `transactions`.
Do not import the seed script over an existing database. For a new, empty
database only, `database/library.sql` creates the three tables, constraints,
one-active-book index and 30 sample members, books and transactions:

```sh
psql -U postgres -d library_db -v ON_ERROR_STOP=1 -f database/library.sql
```

For an existing database missing the index, first check for duplicate active loans:

```sql
SELECT roll_no, count(*)
FROM transactions
WHERE submission_date IS NULL
GROUP BY roll_no
HAVING count(*) > 1;
```

If that query returns no rows, add the index without replacing data:

```sql
CREATE UNIQUE INDEX IF NOT EXISTS one_active_book_per_student
ON transactions (roll_no)
WHERE submission_date IS NULL;
```

A student may have at most one active book, while a title may have several
borrowers when multiple copies are available. Active means
`submission_date IS NULL`; returned transactions remain in history.
Foreign keys prevent deleting members/books referenced by history.

Issuing inserts the loan and decreases stock in one database transaction.
Returning records the submission date and increases stock in one transaction.
Member/book row locks also protect against concurrent issues and returns.

Change Book Settings edits only the total physical copies, using a spinner.
The backend counts active transactions and sets available copies to total minus
currently issued copies, while holding the book row lock. Totals below active
issues are rejected. Availability cannot be edited directly.

Status is `Issued`, `Late` or `Submitted`. Active status and fines are calculated
using PostgreSQL's current date whenever data is refreshed, so stored active
rows do not need daily updates. A due date of today is not late.
Late days × ₹5 is the fine; returning stores the final amount.

The dashboard and student fine totals include recorded returned fines plus
live active fines. There is no payment table or payment-tracking feature, so
the displayed totals do not distinguish paid fines.

## Build and run

Librarian:

```sh
cd librarian
mvn clean compile
mvn exec:java
```

Student (from the repository root):

```sh
cd student
mvn clean compile
mvn exec:java
```

Both apps refresh on navigation; Search, Reset and Refresh buttons reload
current database values. JDBC calls are synchronous to keep this small project
simple; for a large catalogue or slow remote database, move loading to SwingWorker.

## Runnable integration check

From the repository root in PowerShell, with the same connection environment:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/check.ps1
```

The check compiles both apps, creates a uniquely named temporary schema in the
same database, imports the sample SQL, and exercises search, student isolation,
issue/return, rollback, competing issues, stock and live/recorded fines.
It also opens both Swing applications inside a test JVM and exercises navigation,
login, search, details, issue/return and actual scrollbar movement. It closes
those test windows and drops only its own test schema on completion. The database account needs
permission to create/drop that temporary schema. Existing public rows are untouched.
