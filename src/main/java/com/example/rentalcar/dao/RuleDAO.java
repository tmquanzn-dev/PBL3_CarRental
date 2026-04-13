package com.example.rentalcar.dao;

import com.example.rentalcar.models.RuleType;
import com.example.rentalcar.models.Rules;
import com.example.rentalcar.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RuleDAO implements IBaseDAO<Rules, Integer>
{
    // ==========================================================
    // HÀM MAP DỮ LIỆU TỪ MYSQL RA JAVA
    // ==========================================================
    private Rules mapResultSetToRule(ResultSet rs) throws SQLException {
        Rules rule = new Rules();
        rule.setId_rule(rs.getInt("id_rule"));
        rule.setRule_name(rs.getString("rule_name"));

        // Xử lý Enum: Biến "CUOI TUAN" từ DB thành "CUOI_TUAN" cho Java
        String dbRuleType = rs.getString("rule_type");
        if (dbRuleType != null) {
            rule.setRule_type(RuleType.valueOf(dbRuleType.replace(" ", "_")));
        }

        rule.setMulti(rs.getDouble("multi"));

        // LƯU Ý: Ở Model là start_date, nhưng ở DB (file SQL) bạn em lỡ gõ thiếu chữ 't' thành star_date
        // DAO sẽ làm nhiệm vụ "chữa cháy" ghép nối 2 cái tên này lại với nhau!
        rule.setStart_date(rs.getDate("star_date"));
        rule.setEnd_date(rs.getDate("end_date"));

        rule.setIs_active(rs.getBoolean("is_active"));
        return rule;
    }

    // ==========================================================
    // CÁC HÀM CRUD CƠ BẢN
    // ==========================================================
    @Override
    public boolean insert(Rules entity)
    {
        // Chú ý cột star_date trong câu lệnh SQL
        String sql = "INSERT INTO rules (rule_name, rule_type, multi, star_date, end_date, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getRule_name());

            // Xử lý Enum: Đổi "CUOI_TUAN" thành "CUOI TUAN" để lưu xuống DB
            pstm.setString(2, entity.getRule_type().name().replace("_", " "));

            pstm.setDouble(3, entity.getMulti());

            // Lấy thẳng java.sql.Date nhét vào
            pstm.setDate(4, entity.getStart_date());
            pstm.setDate(5, entity.getEnd_date());
            pstm.setBoolean(6, entity.isIs_active());

            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e) { System.err.println("LỖI Thêm Rule: " + e.getMessage()); return false; }
    }

    @Override
    public boolean update(Rules entity)
    {
        String sql = "UPDATE rules SET rule_name = ?, rule_type = ?, multi = ?, star_date = ?, end_date = ?, is_active = ? WHERE id_rule = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getRule_name());
            pstm.setString(2, entity.getRule_type().name().replace("_", " "));
            pstm.setDouble(3, entity.getMulti());
            pstm.setDate(4, entity.getStart_date());
            pstm.setDate(5, entity.getEnd_date());
            pstm.setBoolean(6, entity.isIs_active());
            pstm.setInt(7, entity.getId_rule());
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e) { System.err.println("LỖI Cập nhật Rule: " + e.getMessage()); return false; }
    }

    @Override
    public boolean delete(Integer id)
    {
        // Xóa Mềm: Tắt is_active thay vì xóa hẳn
        String sql = "UPDATE rules SET is_active = 0 WHERE id_rule = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e) { System.err.println("LỖI Khóa Rule: " + e.getMessage()); return false; }
    }

    @Override
    public Rules findById(Integer id)
    {
        String sql = "SELECT * FROM rules WHERE id_rule = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapResultSetToRule(rs);
            }
        }
        catch (SQLException e) { System.err.println("LỖI Tìm Rule: " + e.getMessage()); }
        return null;
    }

    @Override
    public List<Rules> findAll()
    {
        List<Rules> list = new ArrayList<>();
        // Ưu tiên load các luật đang kích hoạt (is_active = 1) lên trước, sắp xếp theo ngày
        String sql = "SELECT * FROM rules ORDER BY is_active DESC, star_date DESC";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery())
        {
            while (rs.next()) list.add(mapResultSetToRule(rs));
        }
        catch (SQLException e) { System.err.println("LỖI Lấy DS Rule: " + e.getMessage()); }
        return list;
    }
}