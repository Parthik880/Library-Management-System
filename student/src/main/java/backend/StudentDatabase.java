package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDatabase {
    private static Connection getConnection() throws SQLException {
        String url = System.getenv("LIBRARY_DB_URL");
        String user = System.getenv("LIBRARY_DB_USER");
        String password = System.getenv("LIBRARY_DB_PASSWORD");
        if (url == null || url.isBlank()) url = "jdbc:postgresql://localhost:5432/library_db";
        if (user == null || user.isBlank()) user = "postgres";
        Connection con = DriverManager.getConnection(url, user, password);
        con.setReadOnly(true);
        con.setAutoCommit(false);
        return con;
    }

    public static boolean studentExists(String rollNo) throws SQLException {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT roll_no FROM members WHERE roll_no = ?")) {
            ps.setString(1, rollNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static List<BookData> getAllBooks(String rollNo) throws SQLException {
        return searchBooks(rollNo, "");
    }

    public static List<BookData> searchBooks(String rollNo, String keyword) throws SQLException {
        String sql = """
                SELECT b.*,
                    CASE WHEN t.transaction_id IS NULL THEN '-'
                         WHEN t.due_date < CURRENT_DATE THEN 'Late' ELSE 'Issued' END AS my_status,
                    COALESCE(GREATEST(CURRENT_DATE - t.due_date, 0) * 5, 0) AS fine,
                    t.issue_date, t.due_date
                FROM books b
                LEFT JOIN transactions t ON b.book_id = t.book_id AND t.roll_no = ?
                    AND t.submission_date IS NULL
                WHERE b.book_id ILIKE ? OR b.title ILIKE ? OR b.author ILIKE ? OR b.category ILIKE ?
                ORDER BY b.title
                """;
        List<BookData> books = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            for (int i = 2; i <= 5; i++) ps.setString(i, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) books.add(readBook(rs));
            }
        }
        return books;
    }

    public static BookData getBookDetails(String rollNo, String bookId) throws SQLException {
        String sql = """
                SELECT b.*,
                    CASE WHEN t.transaction_id IS NULL THEN '-'
                         WHEN t.due_date < CURRENT_DATE THEN 'Late' ELSE 'Issued' END AS my_status,
                    COALESCE(GREATEST(CURRENT_DATE - t.due_date, 0) * 5, 0) AS fine,
                    t.issue_date, t.due_date
                FROM books b
                LEFT JOIN transactions t ON b.book_id = t.book_id AND t.roll_no = ?
                    AND t.submission_date IS NULL
                WHERE b.book_id = ?
                """;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            ps.setString(2, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return readBook(rs);
            }
        }
        return null;
    }

    private static BookData readBook(ResultSet rs) throws SQLException {
        int copies = rs.getInt("available_copies");
        String availability = "Available";
        if (copies == 0) availability = "Unavailable";
        else if (copies == 1) availability = "Low Stock";
        return new BookData(rs.getString("book_id"), rs.getString("title"), rs.getString("author"),
                rs.getString("isbn"), rs.getString("category"), availability, rs.getString("my_status"),
                rs.getDouble("fine"), rs.getString("issue_date"), rs.getString("due_date"));
    }

    // My Books and Fines use the same history query, always scoped to the logged-in roll number.
    public static List<LoanData> getMyBooks(String rollNo) throws SQLException {
        String sql = """
                SELECT t.*, b.title, b.author, b.category,
                    CASE WHEN t.submission_date IS NOT NULL THEN 'Submitted'
                         WHEN t.due_date < CURRENT_DATE THEN 'Late' ELSE 'Issued' END AS live_status,
                    GREATEST(COALESCE(t.submission_date, CURRENT_DATE) - t.due_date, 0) AS late_days,
                    CASE WHEN t.submission_date IS NOT NULL THEN t.fine
                         ELSE GREATEST(CURRENT_DATE - t.due_date, 0) * 5 END AS live_fine
                FROM transactions t JOIN books b ON t.book_id = b.book_id
                WHERE t.roll_no = ? ORDER BY t.transaction_id DESC
                """;
        List<LoanData> loans = new ArrayList<>();
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    loans.add(new LoanData(rs.getLong("transaction_id"), rs.getString("book_id"), rs.getString("title"),
                            rs.getString("author"), rs.getString("category"), rs.getString("issue_date"),
                            rs.getString("due_date"), rs.getString("submission_date"), rs.getString("live_status"),
                            rs.getInt("late_days"), rs.getDouble("live_fine")));
                }
            }
        }
        return loans;
    }

    public record BookData(String bookId, String title, String author, String isbn, String category,
                           String availability, String myStatus, double fine, String issueDate, String dueDate) {}
    public record LoanData(long id, String bookId, String title, String author, String category, String issueDate,
                           String dueDate, String submissionDate, String status, int lateDays, double fine) {}
}
