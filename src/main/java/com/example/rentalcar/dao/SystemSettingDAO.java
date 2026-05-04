package com.example.rentalcar.dao;

import com.example.rentalcar.models.DataType;
import com.example.rentalcar.models.SystemSettings;
import com.example.rentalcar.models.Users;
import com.example.rentalcar.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SystemSettingDAO implements IBaseDAO<SystemSettings, Integer>
{
    // ==========================================================
    // HÀM MAP DỮ LIỆU TỪ MYSQL RA JAVA
    // ==========================================================
    private SystemSettings mapResultSetToSetting(ResultSet rs) throws SQLException
    {
        Users user = new Users();
        user.setId_user(rs.getInt("user_id"));

        Timestamp ts = rs.getTimestamp("update_at");

        return new SystemSettings(
                rs.getInt("id_setting"),
                rs.getString("setting_key"),
                rs.getString("setting_value"),
                DataType.valueOf(rs.getString("data_type")),
                rs.getString("description"),
                rs.getString("category"),
                user,
                ts != null ? ts.toLocalDateTime() : null
        );
    }

    // ==========================================================
    // CÁC HÀM CRUD CƠ BẢN
    // ==========================================================
    @Override
    public boolean insert(SystemSettings entity)
    {
        String sql = "INSERT INTO systemsettings (setting_key, setting_value, data_type, description, category, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getSetting_key());
            pstm.setString(2, entity.getSetting_value());
            pstm.setString(3, entity.getData_type().name());
            pstm.setString(4, entity.getDescription());
            pstm.setString(5, entity.getCategory());
            pstm.setInt(6, entity.getId_user().getId_user());
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e) { System.err.println("LỖI Thêm Setting: " + e.getMessage()); return false; }
    }

    @Override
    public boolean update(SystemSettings entity)
    {
        String sql = "UPDATE systemsettings SET setting_value = ?, user_id = ? WHERE setting_key = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getSetting_value());
            pstm.setInt(2, entity.getId_user().getId_user());
            pstm.setString(3, entity.getSetting_key());
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e) { System.err.println("LỖI Cập nhật Setting: " + e.getMessage()); return false; }
    }

    @Override
    public boolean delete(Integer id)
    {
        String sql = "DELETE FROM systemsettings WHERE id_setting = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e) { System.err.println("LỖI Xóa Setting: " + e.getMessage()); return false; }
    }

    @Override
    public SystemSettings findById(Integer id)
    {
        String sql = "SELECT * FROM systemsettings WHERE id_setting = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapResultSetToSetting(rs);
            }
        }
        catch (SQLException e) { System.err.println("LỖI Tìm Setting theo ID: " + e.getMessage()); }
        return null;
    }

    @Override
    public List<SystemSettings> findAll()
    {
        List<SystemSettings> list = new ArrayList<>();
        String sql = "SELECT * FROM systemsettings";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery())
        {
            while (rs.next()) list.add(mapResultSetToSetting(rs));
        }
        catch (SQLException e) { System.err.println("LỖI Lấy DS Setting: " + e.getMessage()); }
        return list;
    }

    // ==========================================================
    // HÀM NGHIỆP VỤ ĐẶC THÙ
    // ==========================================================

    /**
     * Trả về toàn bộ Object SystemSettings (Dùng khi cần kiểm tra Data Type hoặc Update)
     */
    public SystemSettings findByKey(String key)
    {
        String sql = "SELECT * FROM systemsettings WHERE setting_key = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, key);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapResultSetToSetting(rs);
            }
        }
        catch (SQLException e) { System.err.println("LỖI Tìm Setting theo Key: " + e.getMessage()); }
        return null;
    }

    /**
     * Trả về Nhanh một chuỗi Value (Dùng cho PriceBLL đọc giá xăng, phí phạt...)
     */
    public String getValueByKey(String key)
    {
        String sql = "SELECT setting_value FROM systemsettings WHERE setting_key = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, key);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return rs.getString("setting_value");
            }
        }
        catch (SQLException e) { System.err.println("LỖI Lấy giá trị Setting: " + e.getMessage()); }
        return null;
    }
}