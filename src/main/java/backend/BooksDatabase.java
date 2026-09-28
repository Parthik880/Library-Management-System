package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BooksDatabase {

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


    // =====================================================
    // GET ALL BOOKS
    // =====================================================

    public static List<BookData> getBooks() {

        List<BookData> books =
                new ArrayList<>();

        String sql = """
                SELECT
                    book_id,
                    title,
                    author,
                    category,
                    total_copies,
                    available_copies
                FROM books
                ORDER BY title
                """;

        try (
                Connection con = getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql);
                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                int total =
                        rs.getInt("total_copies");

                int available =
                        rs.getInt("available_copies");


                String status;

                if (available == 0) {

                    status = "Unavailable";

                } else if (available == 1) {

                    status = "Low Stock";

                } else {

                    status = "Available";
                }


                books.add(
                        new BookData(
                                rs.getString("book_id"),
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getString("category"),
                                total,
                                available,
                                status
                        )
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return books;
    }


    // =====================================================
    // ADD BOOK
    // =====================================================

    public static boolean addBook(
            String bookId,
            String title,
            String author,
            String isbn,
            String category,
            int totalCopies) {

        String sql = """
                INSERT INTO books
                (
                    book_id,
                    title,
                    author,
                    isbn,
                    category,
                    total_copies,
                    available_copies
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection con = getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(1, bookId);
            ps.setString(2, title);
            ps.setString(3, author);
            ps.setString(4, isbn);
            ps.setString(5, category);

            ps.setInt(6, totalCopies);

            // when a new book is added,
            // all copies are initially available
            ps.setInt(7, totalCopies);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }


    // =====================================================
    // DELETE BOOK
    // =====================================================

    public static boolean deleteBook(
            String bookId) {

        String sql = """
                DELETE FROM books
                WHERE book_id = ?
                """;

        try (
                Connection con = getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(1, bookId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }


    // =====================================================
    // GET ONE BOOK'S FULL DETAILS
    // =====================================================

    public static BookDetails getBookDetails(
            String bookId) {

        String sql = """
                SELECT
                    book_id,
                    title,
                    author,
                    isbn,
                    category,
                    total_copies,
                    available_copies
                FROM books
                WHERE book_id = ?
                """;

        try (
                Connection con = getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    bookId
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (rs.next()) {

                    int total =
                            rs.getInt(
                                    "total_copies"
                            );

                    int available =
                            rs.getInt(
                                    "available_copies"
                            );

                    int issued =
                            total - available;


                    String status;

                    if (available == 0) {

                        status = "Unavailable";

                    } else if (available == 1) {

                        status = "Low Stock";

                    } else {

                        status = "Available";
                    }


                    return new BookDetails(
                            rs.getString("book_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("isbn"),
                            rs.getString("category"),

                            total,
                            available,
                            issued,

                            status
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return null;
    }


    // =====================================================
    // STUDENTS CURRENTLY ISSUED THIS BOOK
    // =====================================================

    public static List<IssuedStudent>
    getIssuedStudents(
            String bookId) {

        List<IssuedStudent> students =
                new ArrayList<>();


        String sql = """
                SELECT
                    m.roll_no,
                    m.name,

                    t.issue_date,
                    t.due_date,
                    t.status,
                    t.fine

                FROM transactions t

                JOIN members m
                    ON t.roll_no = m.roll_no

                WHERE
                    t.book_id = ?
                    AND t.submission_date IS NULL

                ORDER BY t.issue_date
                """;


        try (
                Connection con = getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    bookId
            );


            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                while (rs.next()) {

                    students.add(
                            new IssuedStudent(

                                    rs.getString(
                                            "roll_no"
                                    ),

                                    rs.getString(
                                            "name"
                                    ),

                                    date(
                                            rs.getDate(
                                                    "issue_date"
                                            )
                                    ),

                                    date(
                                            rs.getDate(
                                                    "due_date"
                                            )
                                    ),

                                    rs.getString(
                                            "status"
                                    ),

                                    rs.getDouble(
                                            "fine"
                                    )
                            )
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }


        return students;
    }


    // =====================================================
    // SEARCH BOOKS
    // =====================================================

    public static List<BookData> searchBooks(
            String keyword) {

        List<BookData> books =
                new ArrayList<>();

        String sql = """
                SELECT
                    book_id,
                    title,
                    author,
                    category,
                    total_copies,
                    available_copies

                FROM books

                WHERE
                    LOWER(title) LIKE ?
                    OR LOWER(author) LIKE ?
                    OR LOWER(book_id) LIKE ?

                ORDER BY title
                """;


        try (
                Connection con = getConnection();
                PreparedStatement ps =
                        con.prepareStatement(sql)
        ) {

            String search =
                    "%" + keyword.toLowerCase() + "%";

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);


            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                while (rs.next()) {

                    int total =
                            rs.getInt(
                                    "total_copies"
                            );

                    int available =
                            rs.getInt(
                                    "available_copies"
                            );


                    String status;

                    if (available == 0) {

                        status = "Unavailable";

                    } else if (available == 1) {

                        status = "Low Stock";

                    } else {

                        status = "Available";
                    }


                    books.add(
                            new BookData(

                                    rs.getString(
                                            "book_id"
                                    ),

                                    rs.getString(
                                            "title"
                                    ),

                                    rs.getString(
                                            "author"
                                    ),

                                    rs.getString(
                                            "category"
                                    ),

                                    total,

                                    available,

                                    status
                            )
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }


        return books;
    }


    // =====================================================
    // HELPER
    // =====================================================

    private static String date(
            Date date) {

        if (date == null) {
            return "-";
        }

        return date.toString();
    }


    // =====================================================
    // MAIN TABLE DATA
    // =====================================================

    public record BookData(

            String bookId,

            String title,

            String author,

            String category,

            int totalCopies,

            int availableCopies,

            String status

    ) {}


    // =====================================================
    // VIEW DETAILS DATA
    // =====================================================

    public record BookDetails(

            String bookId,

            String title,

            String author,

            String isbn,

            String category,

            int totalCopies,

            int availableCopies,

            int issuedCopies,

            String status

    ) {}


    // =====================================================
    // STUDENT DATA INSIDE BOOK DETAILS
    // =====================================================

    public record IssuedStudent(

            String rollNo,

            String name,

            String issueDate,

            String dueDate,

            String status,

            double fine

    ) {}
}