package com.example.rentalcar.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection
{
    // 1. Biến instance duy nhất (Singleton)
    private static DBConnection instance;
    private Connection connection;

    private final String URL = "jdbc:mysql://localhost:3306;databaseName=CarRentalDB;encrypt=true;trustServerCertificate=true;";
    private final String USER = "root";
    private final String PASSWORD = "anhyeuem123";

    // 2. Private Constructor: Ngăn không cho class khác dùng từ khóa 'new DBConnection()'
    private DBConnection()
    {
        try
        {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Kết nối CSDL thành công!");
        }
        catch (SQLException e)
        {
            System.err.println("Lỗi kết nối CSDL: " + e.getMessage());
            throw new RuntimeException("Không thể kết nối đến Database. Vui lòng kiểm tra lại cấu hình.");
        }
    }

    // 3. Phương thức public static để lấy instance duy nhất (Thread-safe)
    public static synchronized DBConnection getInstance()
    {
        try
        {
            // Kiểm tra nếu instance chưa được tạo HOẶC connection đã bị đóng (do rớt mạng, timeout)
            if (instance == null || instance.getConnection().isClosed())
            {
                instance = new DBConnection();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return instance;
    }

    // 4. Trả về đối tượng Connection để thực thi SQL
    public Connection getConnection()
    {
        return connection;
    }
}