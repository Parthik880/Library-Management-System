package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MembersDatabase {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/library_db";

    private static final String USER =
            "postgres";

    private static final String PASSWORD =
            "YOUR_POSTGRES_PASSWORD";


    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }


    // ==========================================
    // GET MEMBERS FOR MAIN TABLE
    // ==========================================

    public static List<MemberData> getMembers() {

        List<MemberData> members =
                new ArrayList<>();

        String sql = """
                SELECT
                    m.name,
                    m.roll_no,

                    COALESCE(
                        t.status,
                        'No Book'
                    ) AS status

                FROM members m

                LEFT JOIN transactions t
                    ON m.roll_no = t.roll_no
                    AND t.submission_date IS NULL

                ORDER BY m.name
                """;


        try (
                Connection con = getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql);
                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                members.add(
                        new MemberData(
                                rs.getString("name"),
                                rs.getString("roll_no"),
                                rs.getString("status")
                        )
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return members;
    }


    // ==========================================
    // GET FULL MEMBER DETAILS
    // ==========================================

    public static MemberDetails getMemberDetails(
            String rollNo) {

        String sql = """
                SELECT
                    m.name,
                    m.roll_no,
                    m.email,
                    m.phone,
                    m.year,
                    m.branch,

                    b.book_id,
                    b.title,

                    t.issue_date,
                    t.due_date,
                    t.submission_date,

                    COALESCE(
                        t.status,
                        'No Book'
                    ) AS status,

                    COALESCE(
                        t.fine,
                        0
                    ) AS fine

                FROM members m

                LEFT JOIN transactions t
                    ON m.roll_no = t.roll_no
                    AND t.submission_date IS NULL

                LEFT JOIN books b
                    ON t.book_id = b.book_id

                WHERE m.roll_no = ?
                """;


        try (
                Connection con = getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    rollNo
            );


            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (rs.next()) {

                    return new MemberDetails(

                            rs.getString("name"),

                            rs.getString("roll_no"),

                            rs.getString("email"),

                            rs.getString("phone"),

                            rs.getInt("year"),

                            rs.getString("branch"),

                            value(
                                    rs.getString("book_id")
                            ),

                            value(
                                    rs.getString("title")
                            ),

                            date(
                                    rs.getDate("issue_date")
                            ),

                            date(
                                    rs.getDate("due_date")
                            ),

                            date(
                                    rs.getDate(
                                            "submission_date"
                                    )
                            ),

                            rs.getString("status"),

                            rs.getDouble("fine")
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return null;
    }


    // ==========================================
    // ADD MEMBER
    // ==========================================

    public static boolean addMember(
            String name,
            String rollNo,
            String email,
            String phone,
            int year,
            String branch) {

        String sql = """
                INSERT INTO members
                (
                    name,
                    roll_no,
                    email,
                    phone,
                    year,
                    branch
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;


        try (
                Connection con = getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(1, name);
            ps.setString(2, rollNo);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setInt(5, year);
            ps.setString(6, branch);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }


    // ==========================================
    // DELETE MEMBER
    // ==========================================

    public static boolean deleteMember(
            String rollNo) {

        String sql = """
                DELETE FROM members
                WHERE roll_no = ?
                """;


        try (
                Connection con = getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    rollNo
            );

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }


    // ==========================================
    // HELPERS
    // ==========================================

    private static String value(
            String value) {

        return value == null
                ? "-"
                : value;
    }


    private static String date(
            Date date) {

        return date == null
                ? "-"
                : date.toString();
    }


    // ==========================================
    // DATA FOR MAIN TABLE
    // ==========================================

    public record MemberData(
            String name,
            String rollNo,
            String status
    ) {}


    // ==========================================
    // DATA FOR VIEW DETAILS
    // ==========================================

    public record MemberDetails(
            String name,
            String rollNo,
            String email,
            String phone,
            int year,
            String branch,

            String bookId,
            String bookName,

            String issueDate,
            String dueDate,
            String submissionDate,

            String status,
            double fine
    ) {}
}