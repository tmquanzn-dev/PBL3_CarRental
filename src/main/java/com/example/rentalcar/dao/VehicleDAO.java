package com.example.rentalcar.dao;

import com.example.rentalcar.utils.DBConnection;
import java.sql.PreparedStatement;
import java.sql.*;

public class VehicleDAO {
    public int getTotalActiveCars() {
        String sql = "Select Count(*) From Vehicles Where status != 'INACTIVE'";
        Connection cnt = DBConnection.getConnection();
        if (cnt == null) {
            System.err.println("LỖI: Không thể kết nối tới Database!");
            return 0;
        }
        try (PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getRentedCars() {
        String sql = "Select Count(*) From Vehicles Where status = 'RENTED'";
        Connection cnt = DBConnection.getConnection();
        if (cnt == null) {
            System.err.println("LỖI: Không thể kết nối tới Database!");
            return 0;
        }
        try (PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getAvailableCars() {
        String sql = "Select Count(*) From Vehicles Where status = 'AVAILABLE'";
        Connection cnt = DBConnection.getConnection();
        if (cnt == null) {
            System.err.println("LỖI: Không thể kết nối tới Database!");
            return 0;
        }
        try (PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
