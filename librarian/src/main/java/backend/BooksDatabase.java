package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BooksDatabase {
    public static List<BookData> getBooks() throws SQLException {
        return searchBooks("");
    }

    public static List<BookData> searchBooks(String keyword) throws SQLException {
        String sql = """
                SELECT * FROM books
                WHERE title ILIKE ? OR author ILIKE ? OR book_id ILIKE ?
                ORDER BY title
                """;
        List<BookData> books = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++) ps.setString(i, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int available = rs.getInt("available_copies");
                    books.add(new BookData(rs.getString("book_id"), rs.getString("title"),
                            rs.getString("author"), rs.getString("category"), rs.getInt("total_copies"),
                            available, availability(available)));
                }
            }
        }
        return books;
    }

    public static boolean addBook(String bookId, String title, String author, String isbn,
                                  String category, int totalCopies) throws SQLException {
        if (bookId.isBlank() || title.isBlank() || author.isBlank() || category.isBlank() || totalCopies < 0) {
            throw new SQLException("Book ID, title, author, category and non-negative copies are required.");
        }
        String sql = """
                INSERT INTO books (book_id, title, author, isbn, category, total_copies, available_copies)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bookId);
            ps.setString(2, title);
            ps.setString(3, author);
            ps.setString(4, isbn.isBlank() ? null : isbn);
            ps.setString(5, category);
            ps.setInt(6, totalCopies);
            ps.setInt(7, totalCopies);
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean deleteBook(String bookId) throws SQLException {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM books WHERE book_id = ?")) {
            ps.setString(1, bookId);
            return ps.executeUpdate() == 1;
        }
    }

    public static void updateTotalCopies(String bookId, int newTotal) throws SQLException {
        if (newTotal < 0) throw new SQLException("Total copies cannot be negative.");
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Use the same book lock as issuing/returning before counting active copies.
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT book_id FROM books WHERE book_id = ? FOR UPDATE")) {
                    ps.setString(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Book not found.");
                    }
                }
                int issued;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) FROM transactions WHERE book_id = ? AND submission_date IS NULL")) {
                    ps.setString(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        issued = rs.getInt(1);
                    }
                }
                if (newTotal < issued) {
                    throw new SQLException("Cannot reduce total copies below the number currently issued.\n"
                            + issued + " copies are currently issued.");
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE books SET total_copies = ?, available_copies = ? WHERE book_id = ?")) {
                    ps.setInt(1, newTotal);
                    ps.setInt(2, newTotal - issued);
                    ps.setString(3, bookId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public static BookDetails getBookDetails(String bookId) throws SQLException {
        String sql = """
                SELECT b.*, (SELECT COUNT(*) FROM transactions t
                    WHERE t.book_id = b.book_id AND t.submission_date IS NULL) AS issued_copies
                FROM books b WHERE b.book_id = ?
                """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_copies");
                    int available = rs.getInt("available_copies");
                    return new BookDetails(rs.getString("book_id"), rs.getString("title"),
                            rs.getString("author"), rs.getString("isbn"), rs.getString("category"),
                            total, available, rs.getInt("issued_copies"), availability(available));
                }
            }
        }
        return null;
    }

    public static List<IssuedStudent> getIssuedStudents(String bookId) throws SQLException {
        String sql = """
                SELECT m.roll_no, m.name, t.issue_date, t.due_date,
                    CASE WHEN t.due_date < CURRENT_DATE THEN 'Late' ELSE 'Issued' END AS status,
                    GREATEST(CURRENT_DATE - t.due_date, 0) * 5 AS fine
                FROM transactions t JOIN members m ON t.roll_no = m.roll_no
                WHERE t.book_id = ? AND t.submission_date IS NULL
                ORDER BY t.issue_date, m.roll_no
                """;
        List<IssuedStudent> students = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(new IssuedStudent(rs.getString("roll_no"), rs.getString("name"),
                            rs.getString("issue_date"), rs.getString("due_date"),
                            rs.getString("status"), rs.getDouble("fine")));
                }
            }
        }
        return students;
    }

    private static String availability(int copies) {
        if (copies == 0) return "Unavailable";
        if (copies == 1) return "Low Stock";
        return "Available";
    }

    public record BookData(String bookId, String title, String author, String category,
                           int totalCopies, int availableCopies, String status) {}
    public record BookDetails(String bookId, String title, String author, String isbn, String category,
                              int totalCopies, int availableCopies, int issuedCopies, String status) {}
    public record IssuedStudent(String rollNo, String name, String issueDate, String dueDate,
                                String status, double fine) {}
}
