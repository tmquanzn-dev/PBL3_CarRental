package com.example.rentalcar.bll;

import com.example.rentalcar.dao.ReportDAO;
import com.example.rentalcar.utils.AppSession;

import java.util.List;
import java.util.Map;

/**
 * StaffReportBLL – Báo cáo cá nhân cho nhân viên (Staff).
 * Không cần checkAdmin() vì đây là dữ liệu của chính họ.
 * Admin cũng có thể dùng để xem báo cáo của bất kỳ nhân viên nào.
 */
public class StaffReportBLL {

    private final ReportDAO reportDAO = new ReportDAO();

    // ─── Guard: chỉ được xem data của chính mình (trừ Admin) ────
    private void checkPermission(int userId) {
        if (AppSession.isAdmin()) return; // Admin xem được tất cả
        if (AppSession.getCurrentUser() == null)
            throw new IllegalStateException("Chưa đăng nhập!");
        if (AppSession.getCurrentUser().getId_user() != userId)
            throw new IllegalStateException("Bạn chỉ được xem báo cáo của chính mình!");
    }

    // ─── Doanh thu theo tháng ────────────────────────────────────
    public Map<Integer, Double> getMonthlyRevenue(int userId, int year) {
        checkPermission(userId);
        return reportDAO.getMonthlyRevenueByStaff(userId, year);
    }

    // ─── Số hợp đồng theo tháng ──────────────────────────────────
    public Map<Integer, Integer> getMonthlyContracts(int userId, int year) {
        checkPermission(userId);
        return reportDAO.getMonthlyContractsByStaff(userId, year);
    }

    // ─── Tổng doanh thu năm ───────────────────────────────────────
    public double getTotalRevenue(int userId, int year) {
        checkPermission(userId);
        return reportDAO.getTotalRevenueByStaff(userId, year);
    }

    // ─── Tổng HĐ hoàn thành năm ──────────────────────────────────
    public int getTotalContracts(int userId, int year) {
        checkPermission(userId);
        return reportDAO.getTotalContractsByStaff(userId, year);
    }

    // ─── Doanh thu tháng cụ thể ──────────────────────────────────
    public double getRevenueByMonth(int userId, int year, int month) {
        checkPermission(userId);
        return reportDAO.getRevenueByStaffAndMonth(userId, year, month);
    }

    // ─── Số HĐ tháng cụ thể ──────────────────────────────────────
    public int getContractsByMonth(int userId, int year, int month) {
        checkPermission(userId);
        return reportDAO.getContractsByStaffAndMonth(userId, year, month);
    }

    // ─── Tổng tiền phạt đã xử lý ─────────────────────────────────
    public double getTotalPenalties(int userId, int year) {
        checkPermission(userId);
        return reportDAO.getTotalPenaltiesByStaff(userId, year);
    }

    // ─── Xếp hạng tháng này ──────────────────────────────────────
    public int getRankThisMonth(int userId) {
        checkPermission(userId);
        return reportDAO.getStaffRankThisMonth(userId);
    }

    // ─── 5 đơn gần nhất ──────────────────────────────────────────
    public List<Map<String, Object>> getRecentContracts(int userId) {
        checkPermission(userId);
        return reportDAO.getRecentContractsByStaff(userId, 5);
    }

    // ─── DT trung bình tháng ─────────────────────────────────────
    public double getAvgMonthlyRevenue(int userId, int year) {
        checkPermission(userId);
        return getTotalRevenue(userId, year) / 12.0;
    }

    // ─── % tăng trưởng DT so với tháng trước ─────────────────────
    public double getGrowthPercent(int userId, int year, int month) {
        checkPermission(userId);
        double current = getRevenueByMonth(userId, year, month);
        int prevYear  = month == 1 ? year - 1 : year;
        int prevMonth = month == 1 ? 12 : month - 1;
        double prev = getRevenueByMonth(userId, prevYear, prevMonth);
        if (prev <= 0) return 0;
        return ((current - prev) / prev) * 100.0;
    }

    // ─── Format helpers (dùng chung với ReportBLL) ───────────────
    public static String formatMoney(double amount) {
        if (amount >= 1_000_000_000)
            return String.format("%.1f tỷ", amount / 1_000_000_000);
        if (amount >= 1_000_000)
            return String.format("%.1f tr", amount / 1_000_000);
        return String.format("%,.0f đ", amount).replace(",", ".");
    }

    public static String formatMoneyFull(double amount) {
        return String.format("%,.0f đ", amount).replace(",", ".");
    }

    public static String formatPercent(double pct) {
        if (pct > 0) return String.format("↗ +%.1f%%", pct);
        if (pct < 0) return String.format("↘ %.1f%%", pct);
        return "→ 0%";
    }
}