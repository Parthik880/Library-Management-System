package backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public static Connection getConnection() throws SQLException {
        String url = System.getenv("LIBRARY_DB_URL");
        String user = System.getenv("LIBRARY_DB_USER");
        String password = System.getenv("LIBRARY_DB_PASSWORD");
        if (url == null || url.isBlank()) url = "jdbc:postgresql://localhost:5432/library_db";
        if (user == null || user.isBlank()) user = "postgres";
        return DriverManager.getConnection(url, user, password);
    }
}
