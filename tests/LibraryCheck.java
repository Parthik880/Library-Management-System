import backend.BooksDatabase;
import backend.Database;
import backend.MembersDatabase;
import backend.StudentDatabase;
import backend.TransactionDatabase;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LibraryCheck {
    private static void check(boolean passed, String message) {
        if (!passed) throw new AssertionError(message);
    }

    static void execute(String sql) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.execute();
        }
    }

    private static void rejectedIssue(String roll, String book, String message) throws SQLException {
        try {
            TransactionDatabase.issueBook(roll, book);
            throw new AssertionError("Issue should fail: " + message);
        } catch (SQLException expected) {
            check(expected.getMessage().contains(message), expected.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        String schema = args[0];
        if (!schema.matches("library_check_[a-f0-9]{32}")) throw new IllegalArgumentException("Test schema only.");
        String url = System.getenv("LIBRARY_DB_URL");
        if (url == null || !url.contains("currentSchema=" + schema)) {
            throw new IllegalArgumentException("Set LIBRARY_DB_URL to the test schema.");
        }
        // Tests create their own schema; public tables and existing rows are never changed.
        execute("CREATE SCHEMA " + schema);
        try {
            for (String sql : Files.readString(Path.of(args[1])).split(";")) {
                if (!sql.isBlank()) execute(sql);
            }
            check(MembersDatabase.getMembers().size() == 30, "30 seed members");
            check(BooksDatabase.getBooks().size() == 30, "30 seed books");
            check(TransactionDatabase.getTransactionHistory().size() == 30, "30 seed transactions");
            check(BooksDatabase.getIssuedStudents("B001").size() == 2, "Multiple copies/borrowers");
            check(StudentDatabase.studentExists("CSE2024001"), "Student login exists");
            check(!StudentDatabase.studentExists("missing"), "Invalid roll rejected");
            check(StudentDatabase.getAllBooks("CSE2024001").size() == 30, "Complete student catalogue");
            check(StudentDatabase.searchBooks("CSE2024001", "Clean Code").size() == 1, "Student search");
            check(StudentDatabase.getMyBooks("CSE2024001").size() == 1, "Student history is scoped");
            check(StudentDatabase.getBookDetails("CSE2024001", "B003").myStatus().equals("-"), "Other borrower's issue hidden");
            check(MembersDatabase.searchMembers("aarav").size() == 1, "Member search");
            check(BooksDatabase.searchBooks("Clean Code").size() == 1, "Book search");
            check(TransactionDatabase.searchTransactionHistory("Aarav").size() == 1, "History search");

            BooksDatabase.updateTotalCopies("B001", 8);
            check(BooksDatabase.getBookDetails("B001").availableCopies() == 6, "Inventory increase derives availability");
            BooksDatabase.updateTotalCopies("B001", 5);
            check(BooksDatabase.getBookDetails("B001").availableCopies() == 3, "Inventory decrease derives availability");
            try {
                BooksDatabase.updateTotalCopies("B001", 1);
                throw new AssertionError("Cannot shrink below two active issues");
            } catch (SQLException expected) {
                check(expected.getMessage().contains("2 copies are currently issued"), "Inventory rejection includes active count");
            }
            check(BooksDatabase.getBookDetails("B001").totalCopies() == 5, "Rejected inventory change preserves total");
            check(BooksDatabase.getBookDetails("B001").availableCopies() == 3, "Rejected inventory change preserves availability");
            BooksDatabase.updateTotalCopies("B001", 2);
            check(BooksDatabase.getBookDetails("B001").availableCopies() == 0, "Inventory may equal active issues");
            BooksDatabase.updateTotalCopies("B001", 5);
            BooksDatabase.updateTotalCopies("B030", 0);
            check(BooksDatabase.getBookDetails("B030").availableCopies() == 0, "Zero inventory allowed without active issues");
            BooksDatabase.updateTotalCopies("B030", 4);
            try {
                BooksDatabase.updateTotalCopies("B030", -1);
                throw new AssertionError("Negative inventory must fail");
            } catch (SQLException expected) {
                check(expected.getMessage().contains("negative"), "Negative inventory validation");
            }
            try {
                BooksDatabase.updateTotalCopies("missing", 4);
                throw new AssertionError("Unknown inventory book must fail");
            } catch (SQLException expected) {
                check(expected.getMessage().contains("Book not found"), "Missing inventory book validation");
            }

            rejectedIssue("missing", "B030", "Student not found");
            rejectedIssue("ME2022030", "missing", "Book not found");
            rejectedIssue("CSE2024001", "B030", "already has");
            execute("UPDATE books SET available_copies = 0 WHERE book_id = 'B030'");
            rejectedIssue("ME2022030", "B030", "No copies");
            execute("UPDATE books SET available_copies = 4 WHERE book_id = 'B030'");

            // Force the stock statement to fail and verify the preceding insert rolls back.
            execute("ALTER TABLE books ADD CONSTRAINT reject_stock CHECK (book_id <> 'B030' OR available_copies <> 3)");
            rejectedIssue("ME2022030", "B030", "reject_stock");
            check(TransactionDatabase.getActiveTransaction("ME2022030") == null, "Issue rollback removes inserted transaction");
            check(BooksDatabase.getBookDetails("B030").availableCopies() == 4, "Issue rollback preserves stock");
            execute("ALTER TABLE books DROP CONSTRAINT reject_stock");
            TransactionDatabase.issueBook("ME2022030", "B030");
            check(BooksDatabase.getBookDetails("B030").availableCopies() == 3, "Stock decreases once");
            TransactionDatabase.TransactionData active = TransactionDatabase.getActiveTransaction("ME2022030");
            check(java.time.LocalDate.parse(active.issueDate()).plusDays(14)
                    .equals(java.time.LocalDate.parse(active.dueDate())), "14-day due date");

            execute("UPDATE transactions SET due_date = CURRENT_DATE WHERE roll_no = 'ME2022030' AND submission_date IS NULL");
            check(TransactionDatabase.getActiveTransaction("ME2022030").fine() == 0, "Due today has zero fine");
            check(TransactionDatabase.getActiveTransaction("ME2022030").status().equals("Issued"), "Due today still Issued");
            execute("UPDATE transactions SET issue_date = CURRENT_DATE - 17, due_date = CURRENT_DATE - 3, status = 'Issued', fine = 0 WHERE roll_no = 'ME2022030' AND submission_date IS NULL");
            check(MembersDatabase.getMemberDetails("ME2022030").fine() == 15, "Member live fine");
            check(MembersDatabase.getMemberDetails("ME2022030").status().equals("Late"), "Member live status");
            check(BooksDatabase.getIssuedStudents("B030").get(0).fine() == 15, "Book details live fine");
            check(TransactionDatabase.getActiveTransaction("ME2022030").fine() == 15, "History live fine");
            check(StudentDatabase.getBookDetails("ME2022030", "B030").fine() == 15, "Student live fine");
            check(StudentDatabase.getMyBooks("ME2022030").get(0).lateDays() == 3, "Student late days");
            check(StudentDatabase.getMyBooks("ME2022030").get(0).fine() == 15, "Student fine details");

            execute("ALTER TABLE books ADD CONSTRAINT reject_return CHECK (book_id <> 'B030' OR available_copies <> 4) NOT VALID");
            try {
                TransactionDatabase.returnBook("ME2022030", "B030");
                throw new AssertionError("Return stock failure must roll back");
            } catch (SQLException expected) {
                check(expected.getMessage().contains("reject_return"), "Return failure reason");
            }
            check(TransactionDatabase.getActiveTransaction("ME2022030") != null, "Return rollback keeps active issue");
            check(BooksDatabase.getBookDetails("B030").availableCopies() == 3, "Return rollback preserves stock");
            execute("ALTER TABLE books DROP CONSTRAINT reject_return");
            check(TransactionDatabase.returnBook("ME2022030", "B030") == 15, "Returned fine is recorded");
            check(TransactionDatabase.getActiveTransaction("ME2022030") == null, "Returned issue inactive");
            check(BooksDatabase.getBookDetails("B030").availableCopies() == 4, "Stock restored once");
            check(StudentDatabase.getMyBooks("ME2022030").size() == 2, "Returned history kept");
            check(StudentDatabase.getMyBooks("ME2022030").get(0).status().equals("Submitted"), "Returned status");
            check(StudentDatabase.getMyBooks("ME2022030").get(0).fine() == 15, "Recorded student fine");
            try {
                TransactionDatabase.returnBook("ME2022030", "B030");
                throw new AssertionError("Duplicate return must fail");
            } catch (SQLException expected) {
                check(BooksDatabase.getBookDetails("B030").availableCopies() == 4, "No duplicate stock increment");
            }
            double expectedFine = 0;
            for (TransactionDatabase.TransactionData row : TransactionDatabase.getTransactionHistory()) expectedFine += row.fine();
            check(TransactionDatabase.getDashboardCounts()[5] == expectedFine, "Dashboard total includes recorded/live fines");

            MembersDatabase.addMember("Test Student A", "CHECK_A", "check.a@example.com", "", 1, "CSE");
            MembersDatabase.addMember("Test Student B", "CHECK_B", "check.b@example.com", "", 1, "IT");
            BooksDatabase.addBook("CHECK_BOOK", "Concurrency Check", "Test Author", "", "Programming", 1);
            concurrentIssue("CHECK_A", "CHECK_B", "CHECK_BOOK");
            String borrower = TransactionDatabase.getActiveTransaction("CHECK_A") != null ? "CHECK_A" : "CHECK_B";
            check(TransactionDatabase.returnBook(borrower, "CHECK_BOOK") == 0, "On-time return zero fine");
            BooksDatabase.addBook("CHECK_BOOK2", "Second Check", "Test Author", "", "Programming", 2);
            concurrentIssue("CHECK_A", "CHECK_A", "CHECK_BOOK2");
            check(BooksDatabase.getBookDetails("CHECK_BOOK2").availableCopies() == 1, "One active issue under concurrency");
            try {
                MembersDatabase.deleteMember("CHECK_A");
                throw new AssertionError("History must prevent deletion");
            } catch (SQLException expected) {
                check("23503".equals(expected.getSQLState()), "Foreign key preserves history");
            }
            BooksDatabase.addBook("CHECK_BOOK3", "Inventory Race", "Test Author", "", "Programming", 1);
            inventoryRace(false);
            check(BooksDatabase.getBookDetails("CHECK_BOOK3").totalCopies() == 3, "Concurrent inventory total");
            check(BooksDatabase.getBookDetails("CHECK_BOOK3").availableCopies() == 2, "Inventory change and issue serialize");
            inventoryRace(true);
            check(BooksDatabase.getBookDetails("CHECK_BOOK3").totalCopies() == 1, "Concurrent inventory decrease");
            check(BooksDatabase.getBookDetails("CHECK_BOOK3").availableCopies() == 1, "Inventory change and return serialize");
            System.out.println("PASS: seed schema, searches, login, isolation, issue/return, rollback, concurrent stock/inventory, live fines and dashboard.");
        } finally {
            execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }

    private static void inventoryRace(boolean returning) throws InterruptedException {
        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread inventory = new Thread(() -> {
            try {
                start.await();
                BooksDatabase.updateTotalCopies("CHECK_BOOK3", returning ? 1 : 3);
            } catch (Exception e) {
                failure.set(e);
            }
        });
        Thread loan = new Thread(() -> {
            try {
                start.await();
                if (returning) TransactionDatabase.returnBook("CHECK_B", "CHECK_BOOK3");
                else TransactionDatabase.issueBook("CHECK_B", "CHECK_BOOK3");
            } catch (Exception e) {
                failure.set(e);
            }
        });
        inventory.start();
        loan.start();
        start.countDown();
        inventory.join(10000);
        loan.join(10000);
        check(!inventory.isAlive() && !loan.isAlive(), "Inventory/loan deadlock");
        check(failure.get() == null, "Inventory/loan race failure: " + failure.get());
    }

    private static void concurrentIssue(String first, String second, String book) throws InterruptedException {
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread[] threads = new Thread[2];
        String[] rolls = {first, second};
        for (int i = 0; i < threads.length; i++) {
            String roll = rolls[i];
            threads[i] = new Thread(() -> {
                try {
                    start.await();
                    TransactionDatabase.issueBook(roll, book);
                    succeeded.incrementAndGet();
                } catch (SQLException expected) {
                    if (!expected.getMessage().contains("No copies") && !expected.getMessage().contains("already has")) {
                        failure.set(expected);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failure.set(e);
                }
            });
            threads[i].start();
        }
        start.countDown();
        for (Thread thread : threads) thread.join(10000);
        for (Thread thread : threads) check(!thread.isAlive(), "Concurrent issue deadlock");
        check(failure.get() == null, "Unexpected concurrent failure: " + failure.get());
        check(succeeded.get() == 1, "Only one competing issue should succeed");
    }
}
