package com.example.rentalcar.dao;

import com.example.rentalcar.models.RuleType;
import com.example.rentalcar.models.Rules;
import com.example.rentalcar.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RuleDAO – CRUD cho bảng rules.
 * Lưu ý: cột ngày trong DB bị lỗi đánh máy → "star_date" (thiếu chữ t)
 * Model Java dùng "start_date" → DAO chịu trách nhiệm ánh xạ hai cái này.
 */
public class RuleDAO implements IBaseDAO<Rules, Integer> {

    // ================================================================
    // MAP DỮ LIỆU DB → JAVA
    // ================================================================
    private Rules mapResultSetToRule(ResultSet rs) throws SQLException {
        Rules rule = new Rules();
        rule.setId_rule(rs.getInt("id_rule"));
        rule.setRule_name(rs.getString("rule_name"));

        String dbRuleType = rs.getString("rule_type");
        if (dbRuleType != null) {
            // DB lưu "CUOI TUAN" / "NGAY LE" / "KHAC"  →  enum CUOI_TUAN / NGAY_LE / KHAC
            rule.setRule_type(RuleType.valueOf(dbRuleType.replace(" ", "_")));
        }

        rule.setMulti(rs.getDouble("multi"));

        // Cột trong DB là "star_date" (typo), model Java gọi là start_date
        rule.setStart_date(rs.getDate("start_date"));
        rule.setEnd_date(rs.getDate("end_date"));
        rule.setIs_active(rs.getBoolean("is_active"));
        return rule;
    }

    // ================================================================
    // INSERT
    // ================================================================
    @Override
    public boolean insert(Rules entity) {
        // Ghi "star_date" vào DB (đúng tên cột)
        String sql = "INSERT INTO rules (rule_name, rule_type, multi, start_date, end_date, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {

            pstm.setString(1, entity.getRule_name());
            // Java enum CUOI_TUAN → DB "CUOI TUAN"
            pstm.setString(2, entity.getRule_type().name().replace("_", " "));
            pstm.setDouble(3, entity.getMulti());
            pstm.setDate(4, entity.getStart_date());
            pstm.setDate(5, entity.getEnd_date());
            pstm.setBoolean(6, entity.isIs_active());

            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI Thêm Rule: " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    // UPDATE
    // ================================================================
    @Override
    public boolean update(Rules entity) {
        String sql = "UPDATE rules SET rule_name=?, rule_type=?, multi=?, start_date=?, end_date=?, is_active=? "
                + "WHERE id_rule=?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {

            pstm.setString(1, entity.getRule_name());
            pstm.setString(2, entity.getRule_type().name().replace("_", " "));
            pstm.setDouble(3, entity.getMulti());
            pstm.setDate(4, entity.getStart_date());
            pstm.setDate(5, entity.getEnd_date());
            pstm.setBoolean(6, entity.isIs_active());
            pstm.setInt(7, entity.getId_rule());

            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI Cập nhật Rule: " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    // DELETE (soft delete – tắt is_active)
    // ================================================================
    @Override
    public boolean delete(Integer id) {
        String sql = "UPDATE rules SET is_active=0 WHERE id_rule=?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {

            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI Tắt Rule: " + e.getMessage());
            return false;
        }
    }

    // Hard delete – dùng khi cần xoá hẳn (admin xác nhận)
    public boolean hardDelete(Integer id) {
        String sql = "DELETE FROM rules WHERE id_rule=?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {

            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI Xóa cứng Rule: " + e.getMessage());
            return false;
        }
    }

    // ================================================================
    // FIND BY ID
    // ================================================================
    @Override
    public Rules findById(Integer id) {
        String sql = "SELECT * FROM rules WHERE id_rule=?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {

            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapResultSetToRule(rs);
            }
        } catch (SQLException e) {
            System.err.println("LỖI Tìm Rule: " + e.getMessage());
        }
        return null;
    }

    // ================================================================
    // FIND ALL – ưu tiên đang bật, sắp xếp theo ngày bắt đầu mới nhất
    // ================================================================
    @Override
    public List<Rules> findAll() {
        List<Rules> list = new ArrayList<>();
        String sql = "SELECT * FROM rules ORDER BY is_active DESC, start_date DESC";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToRule(rs));
        } catch (SQLException e) {
            System.err.println("LỖI Lấy DS Rule: " + e.getMessage());
        }
        return list;
    }

    // ================================================================
    // HÀM NGHIỆP VỤ ĐẶC THÙ
    // ================================================================

    /** Lấy tất cả luật đang bật */
    public List<Rules> findAllActive() {
        List<Rules> list = new ArrayList<>();
        String sql = "SELECT * FROM rules WHERE is_active=1 ORDER BY start_date DESC";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) list.add(mapResultSetToRule(rs));
        } catch (SQLException e) {
            System.err.println("LỖI Lấy DS Rule đang bật: " + e.getMessage());
        }
        return list;
    }

    /** Bật / Tắt nhanh một luật */
    public boolean toggleActive(int id, boolean newState) {
        String sql = "UPDATE rules SET is_active=? WHERE id_rule=?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {

            pstm.setBoolean(1, newState);
            pstm.setInt(2, id);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI Toggle Rule: " + e.getMessage());
            return false;
        }
    }
}