package library;

import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.sql.*;

public class Database {
    static Connection connect() throws SQLException {
        String url = System.getenv("LIBRARY_DB_URL");
        String user = System.getenv("LIBRARY_DB_USER");
        String password = System.getenv("LIBRARY_DB_PASSWORD");
        if (url == null || url.isBlank()) url = "jdbc:postgresql://localhost:5432/library_db";
        if (user == null || user.isBlank()) user = "postgres";
        return DriverManager.getConnection(url, user, password);
    }

    static void loadBooks(DefaultTableModel model, String word) throws SQLException {
        model.setRowCount(0);
        String sql = "SELECT book_id,title,author,category,total_copies,available_copies FROM books "
                + "WHERE book_id ILIKE ? OR title ILIKE ? OR author ILIKE ? OR category ILIKE ? ORDER BY title";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, "%" + word + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String availability = rs.getInt(6) + " available of " + rs.getInt(5);
                    model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), availability});
                }
            }
        }
    }

    static void addBook(String id, String title, String author, String isbn, String category, int copies) throws SQLException {
        if (id.isBlank() || title.isBlank() || author.isBlank() || category.isBlank() || copies < 0)
            throw new SQLException("ID, title, author, category and non-negative copies are required.");
        String sql = "INSERT INTO books (book_id,title,author,isbn,category,total_copies,available_copies) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id.trim()); ps.setString(2, title.trim()); ps.setString(3, author.trim());
            ps.setString(4, isbn.isBlank() ? null : isbn.trim()); ps.setString(5, category.trim());
            ps.setInt(6, copies); ps.setInt(7, copies); ps.executeUpdate();
        }
    }

    static void deleteBook(String id) throws SQLException {
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement("DELETE FROM books WHERE book_id = ?")) {
            ps.setString(1, id);
            if (ps.executeUpdate() == 0) throw new SQLException("Book not found.");
        }
    }

    static int[] getBookStock(String id) throws SQLException {
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement("SELECT total_copies,available_copies FROM books WHERE book_id=?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                int total = rs.getInt(1), available = rs.getInt(2);
                return new int[]{total, total - available, available};
            }
        }
    }

    // Lock the book while active loans are counted, then save matching stock values.
    static void changeAvailability(String id, int newTotal, int newAvailable) throws SQLException {
        if (newTotal < 0 || newAvailable < 0 || newAvailable > newTotal)
            throw new SQLException("Copies must be non-negative, and available copies cannot exceed total copies.");
        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement("SELECT book_id FROM books WHERE book_id=? FOR UPDATE")) {
                    ps.setString(1, id); try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) throw new SQLException("Book not found."); }
                }
                int issued;
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM transactions WHERE book_id=? AND submission_date IS NULL")) {
                    ps.setString(1, id); try (ResultSet rs = ps.executeQuery()) { rs.next(); issued = rs.getInt(1); }
                }
                if (newTotal < issued) throw new SQLException("New total copies cannot be less than " + issued + " currently issued copies.");
                int expectedAvailable = newTotal - issued;
                if (newAvailable != expectedAvailable)
                    throw new SQLException("New available copies must be " + expectedAvailable + " for the current issued copies.");
                try (PreparedStatement ps = con.prepareStatement("UPDATE books SET total_copies=?,available_copies=? WHERE book_id=?")) {
                    ps.setInt(1, newTotal); ps.setInt(2, newAvailable); ps.setString(3, id); ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) { con.rollback(); throw e; }
        }
    }

    static void loadMembers(DefaultTableModel model, String word) throws SQLException {
        model.setRowCount(0);
        String sql = "SELECT roll_no,name,email,phone,year,branch FROM members "
                + "WHERE roll_no ILIKE ? OR name ILIKE ? OR email ILIKE ? ORDER BY name";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++) ps.setString(i, "%" + word + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getInt(5), rs.getString(6)});
            }
        }
    }

    static void addMember(String roll, String name, String email, String phone, int year, String branch) throws SQLException {
        if (roll.isBlank() || name.isBlank() || email.isBlank() || branch.isBlank() || year < 1 || year > 10)
            throw new SQLException("Roll, name, email, branch and a year from 1 to 10 are required.");
        String sql = "INSERT INTO members (roll_no,name,email,phone,year,branch) VALUES (?,?,?,?,?,?)";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roll.trim()); ps.setString(2, name.trim()); ps.setString(3, email.trim());
            ps.setString(4, phone.trim()); ps.setInt(5, year); ps.setString(6, branch.trim()); ps.executeUpdate();
        }
    }

    static void deleteMember(String roll) throws SQLException {
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement("DELETE FROM members WHERE roll_no = ?")) {
            ps.setString(1, roll);
            if (ps.executeUpdate() == 0) throw new SQLException("Member not found.");
        }
    }

    static void loadActiveIssue(DefaultTableModel model, String roll) throws SQLException {
        model.setRowCount(0);
        String sql = "SELECT t.book_id,b.title,t.issue_date,t.due_date," +
                "CASE WHEN t.due_date < CURRENT_DATE THEN 'Late' ELSE 'Issued' END," +
                "GREATEST(CURRENT_DATE-t.due_date,0)*5 FROM transactions t " +
                "JOIN books b ON b.book_id=t.book_id WHERE t.roll_no=? AND t.submission_date IS NULL";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roll);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getDate(3),
                        rs.getDate(4), rs.getString(5), "₹" + rs.getDouble(6)});
            }
        }
    }

    // The loan row and the available-copy change succeed or fail together.
    static void issueBook(String roll, String book, LocalDate issueDate, LocalDate dueDate) throws SQLException {
        if (roll.isBlank() || book.isBlank()) throw new SQLException("Enter a member roll number and book ID.");
        if (issueDate == null || dueDate == null || dueDate.isBefore(issueDate))
            throw new SQLException("Due date cannot be before the issue date.");
        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try {
                requireMember(con, roll.trim());
                try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM transactions WHERE roll_no=? AND submission_date IS NULL")) {
                    ps.setString(1, roll.trim());
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) throw new SQLException("This member already has an active book."); }
                }
                try (PreparedStatement ps = con.prepareStatement("SELECT available_copies FROM books WHERE book_id=? FOR UPDATE")) {
                    ps.setString(1, book.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Book not found.");
                        if (rs.getInt(1) < 1) throw new SQLException("No copies are available.");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO transactions (roll_no,book_id,issue_date,due_date,status,fine) VALUES (?,?,?,?, 'Issued',0)")) {
                    ps.setString(1, roll.trim()); ps.setString(2, book.trim());
                    ps.setDate(3, Date.valueOf(issueDate)); ps.setDate(4, Date.valueOf(dueDate)); ps.executeUpdate();
                }
                changeCopies(con, book.trim(), -1); con.commit();
            } catch (SQLException e) { con.rollback(); throw e; }
        }
    }

    // Return saves the final fine and restores one available copy atomically.
    static double returnBook(String roll, String book) throws SQLException {
        if (roll.isBlank() || book.isBlank()) throw new SQLException("Enter a member roll number and book ID.");
        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try {
                requireMember(con, roll.trim());
                long id; double fine;
                String find = "SELECT transaction_id,GREATEST(CURRENT_DATE-due_date,0)*5 FROM transactions "
                        + "WHERE roll_no=? AND book_id=? AND submission_date IS NULL FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(find)) {
                    ps.setString(1, roll.trim()); ps.setString(2, book.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("No active issue matches this member and book.");
                        id = rs.getLong(1); fine = rs.getDouble(2);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE transactions SET submission_date=CURRENT_DATE,status='Submitted',fine=? WHERE transaction_id=?")) {
                    ps.setDouble(1, fine); ps.setLong(2, id); ps.executeUpdate();
                }
                changeCopies(con, book.trim(), 1); con.commit(); return fine;
            } catch (SQLException e) { con.rollback(); throw e; }
        }
    }

    private static void requireMember(Connection con, String roll) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM members WHERE roll_no=? FOR UPDATE")) {
            ps.setString(1, roll);
            try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) throw new SQLException("Member not found."); }
        }
    }

    private static void changeCopies(Connection con, String book, int amount) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE books SET available_copies=available_copies+? WHERE book_id=?")) {
            ps.setInt(1, amount); ps.setString(2, book);
            if (ps.executeUpdate() != 1) throw new SQLException("Book stock could not be updated.");
        }
    }
}
