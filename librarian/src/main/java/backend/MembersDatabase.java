package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MembersDatabase {
    public static List<MemberData> getMembers() throws SQLException {
        return searchMembers("");
    }

    public static List<MemberData> searchMembers(String keyword) throws SQLException {
        String sql = """
                SELECT m.name, m.roll_no,
                    CASE WHEN t.transaction_id IS NULL THEN 'No Book'
                         WHEN t.due_date < CURRENT_DATE THEN 'Late' ELSE 'Issued' END AS status
                FROM members m
                LEFT JOIN transactions t ON m.roll_no = t.roll_no AND t.submission_date IS NULL
                WHERE m.name ILIKE ? OR m.roll_no ILIKE ?
                ORDER BY m.name
                """;
        List<MemberData> members = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(new MemberData(rs.getString("name"), rs.getString("roll_no"),
                            rs.getString("status")));
                }
            }
        }
        return members;
    }

    public static MemberDetails getMemberDetails(String rollNo) throws SQLException {
        String sql = """
                SELECT m.*, b.book_id, b.title, t.issue_date, t.due_date, t.submission_date,
                    CASE WHEN t.transaction_id IS NULL THEN 'No Book'
                         WHEN t.due_date < CURRENT_DATE THEN 'Late' ELSE 'Issued' END AS status,
                    COALESCE(GREATEST(CURRENT_DATE - t.due_date, 0) * 5, 0) AS fine
                FROM members m
                LEFT JOIN transactions t ON m.roll_no = t.roll_no AND t.submission_date IS NULL
                LEFT JOIN books b ON t.book_id = b.book_id
                WHERE m.roll_no = ?
                """;
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new MemberDetails(rs.getString("name"), rs.getString("roll_no"),
                            rs.getString("email"), rs.getString("phone"), rs.getInt("year"),
                            rs.getString("branch"), rs.getString("book_id"), rs.getString("title"),
                            rs.getString("issue_date"), rs.getString("due_date"),
                            rs.getString("submission_date"), rs.getString("status"), rs.getDouble("fine"));
                }
            }
        }
        return null;
    }

    public static boolean addMember(String name, String rollNo, String email, String phone,
                                    int year, String branch) throws SQLException {
        if (name.isBlank() || rollNo.isBlank() || email.isBlank() || branch.isBlank()
                || year < 1 || year > 10) {
            throw new SQLException("Name, roll number, email, branch and a year from 1 to 10 are required.");
        }
        String sql = "INSERT INTO members (name, roll_no, email, phone, year, branch) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, rollNo);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setInt(5, year);
            ps.setString(6, branch);
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean deleteMember(String rollNo) throws SQLException {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM members WHERE roll_no = ?")) {
            ps.setString(1, rollNo);
            return ps.executeUpdate() == 1;
        }
    }

    public record MemberData(String name, String rollNo, String status) {}
    public record MemberDetails(String name, String rollNo, String email, String phone, int year,
                                String branch, String bookId, String bookName, String issueDate,
                                String dueDate, String submissionDate, String status, double fine) {}
}
