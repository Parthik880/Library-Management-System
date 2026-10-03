package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDatabase {
    private static final String HISTORY_SQL = """
            SELECT t.transaction_id, t.roll_no, m.name, t.book_id, b.title,
                   t.issue_date, t.due_date, t.submission_date,
                   CASE WHEN t.submission_date IS NOT NULL THEN 'Submitted'
                        WHEN t.due_date < CURRENT_DATE THEN 'Late' ELSE 'Issued' END AS status,
                   CASE WHEN t.submission_date IS NOT NULL THEN t.fine
                        ELSE GREATEST(CURRENT_DATE - t.due_date, 0) * 5 END AS fine
            FROM transactions t
            JOIN members m ON t.roll_no = m.roll_no
            JOIN books b ON t.book_id = b.book_id
            """;

    public static void issueBook(String rollNo, String bookId) throws SQLException {
        if (rollNo.isBlank() || bookId.isBlank()) throw new SQLException("Enter a roll number and book ID.");
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try {
                lockMember(con, rollNo);
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT transaction_id FROM transactions WHERE roll_no = ? AND submission_date IS NULL")) {
                    ps.setString(1, rollNo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new SQLException("This student already has an active book.");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT available_copies FROM books WHERE book_id = ? FOR UPDATE")) {
                    ps.setString(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Book not found.");
                        if (rs.getInt(1) <= 0) throw new SQLException("No copies are available.");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("""
                        INSERT INTO transactions (roll_no, book_id, issue_date, due_date, submission_date, status, fine)
                        VALUES (?, ?, CURRENT_DATE, CURRENT_DATE + 14, NULL, 'Issued', 0)
                        """)) {
                    ps.setString(1, rollNo);
                    ps.setString(2, bookId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ?")) {
                    ps.setString(1, bookId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public static double returnBook(String rollNo, String bookId) throws SQLException {
        if (rollNo.isBlank() || bookId.isBlank()) throw new SQLException("Enter a roll number and book ID.");
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try {
                lockMember(con, rollNo);
                long id;
                double fine;
                try (PreparedStatement ps = con.prepareStatement("""
                        SELECT transaction_id, GREATEST(CURRENT_DATE - due_date, 0) * 5 AS fine
                        FROM transactions WHERE roll_no = ? AND book_id = ? AND submission_date IS NULL
                        FOR UPDATE
                        """)) {
                    ps.setString(1, rollNo);
                    ps.setString(2, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("No active issue matches this student and book.");
                        id = rs.getLong("transaction_id");
                        fine = rs.getDouble("fine");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE transactions SET submission_date = CURRENT_DATE, status = 'Submitted', fine = ?
                        WHERE transaction_id = ?
                        """)) {
                    ps.setDouble(1, fine);
                    ps.setLong(2, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ?")) {
                    ps.setString(1, bookId);
                    if (ps.executeUpdate() != 1) throw new SQLException("Book stock could not be updated.");
                }
                con.commit();
                return fine;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // Lock the member first so concurrent requests cannot issue two active books.
    private static void lockMember(Connection con, String rollNo) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT roll_no FROM members WHERE roll_no = ? FOR UPDATE")) {
            ps.setString(1, rollNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Student not found.");
            }
        }
    }

    public static TransactionData getActiveTransaction(String rollNo) throws SQLException {
        List<TransactionData> rows = readHistory(" WHERE t.roll_no = ? AND t.submission_date IS NULL", rollNo, 1);
        if (rows.isEmpty()) return null;
        return rows.get(0);
    }

    public static List<TransactionData> getTransactionHistory() throws SQLException {
        return searchTransactionHistory("");
    }

    public static List<TransactionData> searchTransactionHistory(String keyword) throws SQLException {
        return readHistory(" WHERE t.roll_no ILIKE ? OR m.name ILIKE ? OR t.book_id ILIKE ? OR b.title ILIKE ?",
                "%" + keyword + "%", 4);
    }

    private static List<TransactionData> readHistory(String where, String value, int parameters) throws SQLException {
        List<TransactionData> rows = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(HISTORY_SQL + where + " ORDER BY t.transaction_id DESC")) {
            for (int i = 1; i <= parameters; i++) ps.setString(i, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new TransactionData(rs.getLong("transaction_id"), rs.getString("roll_no"),
                            rs.getString("name"), rs.getString("book_id"), rs.getString("title"),
                            rs.getString("issue_date"), rs.getString("due_date"), rs.getString("submission_date"),
                            rs.getString("status"), rs.getDouble("fine")));
                }
            }
        }
        return rows;
    }

    public static double[] getDashboardCounts() throws SQLException {
        String sql = """
                SELECT (SELECT count(*) FROM members),
                       (SELECT count(*) FROM books),
                       (SELECT COALESCE(sum(available_copies), 0) FROM books),
                       count(*) FILTER (WHERE submission_date IS NULL),
                       count(*) FILTER (WHERE submission_date IS NULL AND due_date < CURRENT_DATE),
                       COALESCE(sum(CASE WHEN submission_date IS NOT NULL THEN fine
                           ELSE GREATEST(CURRENT_DATE - due_date, 0) * 5 END), 0)
                FROM transactions
                """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            double[] counts = new double[6];
            for (int i = 0; i < counts.length; i++) counts[i] = rs.getDouble(i + 1);
            return counts;
        }
    }

    public record TransactionData(long id, String rollNo, String name, String bookId, String title,
                                  String issueDate, String dueDate, String submissionDate,
                                  String status, double fine) {}
}
