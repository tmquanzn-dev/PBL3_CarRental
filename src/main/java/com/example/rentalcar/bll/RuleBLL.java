package com.example.rentalcar.bll;

import com.example.rentalcar.dao.RuleDAO;
import com.example.rentalcar.models.RuleType;
import com.example.rentalcar.models.Rules;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * RuleBLL – Nghiệp vụ quản lý luật tính giá.
 * Chỉ Admin mới được thao tác (kiểm tra phân quyền thực hiện ở Controller).
 */
public class RuleBLL {

    private final RuleDAO ruleDAO = new RuleDAO();

    // ================================================================
    // TRUY VẤN
    // ================================================================

    public List<Rules> getAllRules() {
        return ruleDAO.findAll();
    }

    public List<Rules> getActiveRules() {
        return ruleDAO.findAllActive();
    }

    public Rules getRuleById(int id) {
        return ruleDAO.findById(id);
    }

    // ================================================================
    // THÊM LUẬT MỚI
    // ================================================================
    public boolean addRule(Rules rule) {
        validateRule(rule);
        return ruleDAO.insert(rule);
    }

    // ================================================================
    // CẬP NHẬT LUẬT
    // ================================================================
    public boolean updateRule(Rules rule) {
        validateRule(rule);
        Rules existing = ruleDAO.findById(rule.getId_rule());
        if (existing == null)
            throw new IllegalArgumentException("Không tìm thấy luật với ID: " + rule.getId_rule());
        return ruleDAO.update(rule);
    }

    // ================================================================
    // XÓA LUẬT (xóa cứng)
    // ================================================================
    public boolean deleteRule(int id) {
        Rules rule = ruleDAO.findById(id);
        if (rule == null)
            throw new IllegalArgumentException("Không tìm thấy luật để xóa!");
        return ruleDAO.hardDelete(id);
    }

    // ================================================================
    // BẬT / TẮT NHANH
    // ================================================================
    public boolean toggleRule(int id) {
        Rules rule = ruleDAO.findById(id);
        if (rule == null)
            throw new IllegalArgumentException("Không tìm thấy luật!");
        return ruleDAO.toggleActive(id, !rule.isIs_active());
    }

    // ================================================================
    // VALIDATE DỮ LIỆU ĐẦU VÀO
    // ================================================================
    private void validateRule(Rules rule) {
        if (rule.getRule_name() == null || rule.getRule_name().isBlank())
            throw new IllegalArgumentException("Tên luật không được để trống!");

        if (rule.getRule_type() == null)
            throw new IllegalArgumentException("Vui lòng chọn loại luật!");

        if (rule.getMulti() <= 0)
            throw new IllegalArgumentException("Hệ số nhân phải lớn hơn 0!");

        if (rule.getMulti() > 10)
            throw new IllegalArgumentException("Hệ số nhân không được vượt quá 10!");

        if (rule.getStart_date() == null || rule.getEnd_date() == null)
            throw new IllegalArgumentException("Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc!");

        if (rule.getEnd_date().before(rule.getStart_date()))
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");
    }

    // ================================================================
    // THỐNG KÊ (dùng cho cards ở màn hình quản lý)
    // ================================================================

    public int getTotalRules() {
        return ruleDAO.findAll().size();
    }

    public long getActiveCount() {
        return ruleDAO.findAll().stream().filter(Rules::isIs_active).count();
    }

    /** Kiểm tra xem hôm nay có luật nào đang áp dụng không */
    public long getApplicableTodayCount() {
        Date today = Date.valueOf(LocalDate.now());
        return ruleDAO.findAllActive().stream()
                .filter(r -> r.getStart_date() != null && r.getEnd_date() != null
                        && !today.before(r.getStart_date())
                        && !today.after(r.getEnd_date()))
                .count();
    }

    // ================================================================
    // FORMAT HỆ SỐ NHÂN (hiển thị lên UI)
    // ================================================================
    public static String formatMultiplier(double multi) {
        return String.format("x%.2f", multi);
    }
}