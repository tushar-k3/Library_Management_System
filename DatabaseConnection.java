package librarymanagementsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton JDBC connection.
 * EDIT: DB_URL, USER, PASSWORD to match your MySQL setup.
 */
public class DatabaseConnection {

    private static final String DB_URL  =
        "jdbc:mysql://localhost:3306/db" +
        "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";   // <-- your username
    private static final String PASSWORD = "26092006";   // <-- your password

    private static Connection conn = null;

    private DatabaseConnection() {}

    public static Connection get() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                    "MySQL JDBC Driver not found.\nAdd mysql-connector-j-*.jar to the build path.", e);
            }
        }
        return conn;
    }

    public static void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignored) {}
    }
}
