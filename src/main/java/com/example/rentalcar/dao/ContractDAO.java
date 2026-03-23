package com.example.rentalcar.dao;

import com.example.rentalcar.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ContractDAO {
    public double getTodayRevenue() {
        String sql = "Select Coalesce(Sum(total_price),0) from contracts " +
                "Where Date(start_datetime) = Curdate() " +
                "And status = 'HOAN THANH'";
        try (Connection cnt = DBConnection.getConnection()) {
            if (cnt == null) {
                System.err.println("Lỗi! Không thể kết nối tới Database");
                return 0.0;
            }
            try (PreparedStatement pstm = cnt.prepareStatement(sql);
                 ResultSet rs = pstm.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        }
        catch (SQLException e) {
            System.err.println("Lỗi truy vấn doanh thu: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }
}
