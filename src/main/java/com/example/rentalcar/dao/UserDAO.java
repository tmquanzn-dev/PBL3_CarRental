package com.example.rentalcar.dao;

import com.example.rentalcar.models.Users;
import com.example.rentalcar.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IBaseDAO<Users, Integer>
{
    // Hàm phụ trợ giúp parse dữ liệu từ ResultSet ra đối tượng Users
    private Users mapResultSetToUser(ResultSet rs) throws SQLException
    {
        Users user = new Users();
        user.setId_user(rs.getInt("id_user"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setFull_name(rs.getString("full_name"));
        user.setPhone(rs.getString("phone"));
        user.setCccd(rs.getString("cccd"));
        user.setGender(rs.getBoolean("gender"));
        user.setBirth_date(rs.getDate("birth_date")); // Lấy java.sql.Date tự động cast về java.util.Date
        user.setEmail(rs.getString("email"));
        user.setIs_active(rs.getBoolean("is_active"));
        user.setAddress(rs.getString("address"));
        return user;
    }

    public Users findByUsername(String username)
    {
        String sql = "SELECT * FROM Users WHERE username = ? AND is_active = 1";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, username);
            try (ResultSet rs = pstm.executeQuery())
            {
                if (rs.next())
                {
                    return mapResultSetToUser(rs);
                }
            }
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Tìm User theo Username: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean insert(Users entity)
    {
        String sql = "INSERT INTO Users (username, password, role, full_name, phone, cccd, gender, birth_date, email, is_active, address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?)";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getUsername());
            pstm.setString(2, entity.getPassword());
            pstm.setString(3, entity.getRole());
            pstm.setString(4, entity.getFull_name());
            pstm.setString(5, entity.getPhone());
            pstm.setString(6, entity.getCccd());
            pstm.setBoolean(7, entity.isGender());

            // Xử lý an toàn cho birth_date (chuyển từ util.Date sang sql.Date)
            if (entity.getBirth_date() != null)
                pstm.setDate(8, new java.sql.Date(entity.getBirth_date().getTime()));
            else
                pstm.setNull(8, java.sql.Types.DATE);

            pstm.setString(9, entity.getEmail());
            pstm.setString(10, entity.getAddress());

            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Thêm User mới: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Users entity)
    {
        // Không cho phép update username
        String sql = "UPDATE Users SET password = ?, role = ?, full_name = ?, phone = ?, cccd = ?, gender = ?, birth_date = ?, email = ?, is_active = ?, address = ? WHERE id_user = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getPassword());
            pstm.setString(2, entity.getRole());
            pstm.setString(3, entity.getFull_name());
            pstm.setString(4, entity.getPhone());
            pstm.setString(5, entity.getCccd());
            pstm.setBoolean(6, entity.isGender());

            if (entity.getBirth_date() != null)
                pstm.setDate(7, new java.sql.Date(entity.getBirth_date().getTime()));
            else
                pstm.setNull(7, java.sql.Types.DATE);

            pstm.setString(8, entity.getEmail());
            pstm.setBoolean(9, entity.isIs_active());
            pstm.setString(10, entity.getAddress());
            pstm.setInt(11, entity.getId_user());

            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Cập nhật User: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Integer id)
    {
        String sql = "UPDATE Users SET is_active = 0 WHERE id_user = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Khóa User: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Users findById(Integer id)
    {
        String sql = "SELECT * FROM Users WHERE id_user = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery())
            {
                if (rs.next())
                {
                    return mapResultSetToUser(rs);
                }
            }
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Tìm User theo ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Users> findAll()
    {
        List<Users> listUsers = new ArrayList<>();
        String sql = "SELECT * FROM Users";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery())
        {
            while (rs.next())
            {
                listUsers.add(mapResultSetToUser(rs));
            }
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Lấy danh sách toàn bộ User: " + e.getMessage());
        }
        return listUsers;
    }
}