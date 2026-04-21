package com.example.rentalcar.dao;

import com.example.rentalcar.models.Roles;
import com.example.rentalcar.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO implements IBaseDAO<Roles, Integer>
{
    private Roles mapResultSetToRole(ResultSet rs) throws SQLException {
        Roles role = new Roles();
        role.setRole_id(rs.getInt("role_id"));
        role.setRole_name(rs.getString("role_name"));
        role.setDescription(rs.getString("description"));
        return role;
    }

    @Override
    public boolean insert(Roles entity)
    {
        String sql = "INSERT INTO roles (role_name, description) VALUES (?, ?)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getRole_name());
            pstm.setString(2, entity.getDescription());
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e) { System.err.println("LỖI Thêm Role: " + e.getMessage()); return false; }
    }

    @Override
    public boolean update(Roles entity)
    {
        String sql = "UPDATE roles SET role_name = ?, description = ? WHERE role_id = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getRole_name());
            pstm.setString(2, entity.getDescription());
            pstm.setInt(3, entity.getRole_id());
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e) { System.err.println("LỖI Cập nhật Role: " + e.getMessage()); return false; }
    }

    @Override
    public boolean delete(Integer id)
    {
        // Xóa cứng: Cẩn thận vì nếu Role này đang có User sử dụng sẽ sinh lỗi Khóa ngoại
        String sql = "DELETE FROM roles WHERE role_id = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e) { System.err.println("LỖI Xóa Role: " + e.getMessage()); return false; }
    }

    @Override
    public Roles findById(Integer id)
    {
        String sql = "SELECT * FROM roles WHERE role_id = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapResultSetToRole(rs);
            }
        }
        catch (SQLException e) { System.err.println("LỖI Tìm Role: " + e.getMessage()); }
        return null;
    }

    @Override
    public List<Roles> findAll()
    {
        List<Roles> list = new ArrayList<>();
        String sql = "SELECT * FROM roles";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery())
        {
            while (rs.next()) list.add(mapResultSetToRole(rs));
        }
        catch (SQLException e) { System.err.println("LỖI Lấy DS Role: " + e.getMessage()); }
        return list;
    }
}