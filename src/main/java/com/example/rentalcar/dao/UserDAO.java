package com.example.rentalcar.dao;

import com.example.rentalcar.models.Users;
import com.example.rentalcar.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public Users login(String username, String password) {
        String sql = "Select * From Users Where username = ? And password = ? And is_active = 1";

        Connection cnt = DBConnection.getConnection();
        if (cnt == null) {
            System.err.println("LỖI: Không thể kết nối tới Database!");
            return null;
        }
        try (PreparedStatement pstm = cnt.prepareStatement(sql)){
            pstm.setString(1, username);
            pstm.setString(2, password);

            try(ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    Users user = new Users();
                    user.setId_user(rs.getInt("id_user"));
                    user.setUsername(rs.getString("username"));
                    user.setFull_name(rs.getString("full_name"));
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn SQL: " + e.getMessage());
        }
        return null;
    }
}
