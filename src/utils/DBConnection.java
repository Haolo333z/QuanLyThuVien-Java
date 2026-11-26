package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=LibraryManagementDB;"
            + "encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456"; // Thay bằng mật khẩu sa trên máy Hào để test

    public static Connection requireConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy SQLServerDriver!", e);
        }
    }

    public static Connection getConnection() {
        try {
            return requireConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Hàm main để chạy test kết nối ngay trên Eclipse
    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            System.out.println("Ket noi LibraryManagementDB thanh cong!");
            closeConnection(conn);
        } else {
            System.out.println("Ket noi that bai! Kiem tra lai TCP/IP, port 1433 hoac mat khau sa.");
        }
    }
}