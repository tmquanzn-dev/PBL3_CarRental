package com.example.rentalcar.bll;

import com.example.rentalcar.dao.ReportDAO;
import com.example.rentalcar.models.StaffReportRow;
import com.example.rentalcar.models.VehicleReportRow;
import com.example.rentalcar.utils.AppSession;

import java.util.List;
import java.util.Map;

public class ReportBLL {

    private final ReportDAO reportDAO = new ReportDAO();

    // ─── Guard phân quyền ────────────────────────────────
    private void checkAdmin() {
        if (!AppSession.isAdmin())
            throw new IllegalStateException("Cảnh báo: Dữ liệu báo cáo chỉ dành cho Quản trị viên!");
    }

    // ─── Doanh thu theo tháng (BarChart) ─────────────────
    public Map<Integer, Double> getMonthlyRevenue(int year) {
        checkAdmin();
        return reportDAO.getMonthlyRevenue(year);
    }

    // ─── Tổng doanh thu năm ───────────────────────────────
    public double getTotalRevenue(int year) {
        checkAdmin();
        return reportDAO.getTotalRevenueByYear(year);
    }

    // ─── Doanh thu tháng cụ thể ──────────────────────────
    public double getRevenueByMonth(int year, int month) {
        checkAdmin();
        return reportDAO.getRevenueByMonth(year, month);
    }

    // ─── % tăng trưởng doanh thu so với năm trước ────────
    public double getRevenueGrowthPercent(int year) {
        checkAdmin();
        double current = reportDAO.getTotalRevenueByYear(year);
        double prev    = reportDAO.getTotalRevenueByYear(year - 1);
        if (prev <= 0) return 0;
        return ((current - prev) / prev) * 100.0;
    }

    // ─── % tăng trưởng tháng so với tháng trước ──────────
    public double getMonthGrowthPercent(int year, int month) {
        checkAdmin();
        double current = reportDAO.getRevenueByMonth(year, month);
        int prevYear  = month == 1 ? year - 1 : year;
        int prevMonth = month == 1 ? 12 : month - 1;
        double prev   = reportDAO.getRevenueByMonth(prevYear, prevMonth);
        if (prev <= 0) return 0;
        return ((current - prev) / prev) * 100.0;
    }

    // ─── Tổng HĐ hoàn thành năm ──────────────────────────
    public int getTotalContracts(int year) {
        checkAdmin();
        return reportDAO.getTotalContractsByYear(year);
    }

    // ─── Số HĐ tháng cụ thể ──────────────────────────────
    public int getContractsByMonth(int year, int month) {
        checkAdmin();
        return reportDAO.getContractsByMonth(year, month);
    }

    // ─── Tổng khách hàng năm ─────────────────────────────
    public int getTotalCustomers(int year) {
        checkAdmin();
        return reportDAO.getTotalCustomersByYear(year);
    }

    // ─── Khách hàng tháng cụ thể ─────────────────────────
    public int getCustomersByMonth(int year, int month) {
        checkAdmin();
        return reportDAO.getCustomersByMonth(year, month);
    }

    // ─── DT trung bình tháng ─────────────────────────────
    public double getAvgMonthlyRevenue(int year) {
        checkAdmin();
        return getTotalRevenue(year) / 12.0;
    }

    // ─── Phân bố trạng thái HĐ ───────────────────────────
    public Map<String, Integer> getContractStatusDistribution(int year) {
        checkAdmin();
        return reportDAO.getContractStatusDistribution(year);
    }

    // ─── Top 5 xe - NĂM ──────────────────────────────────
    public List<VehicleReportRow> getTopVehiclesByYear(int year) {
        checkAdmin();
        return reportDAO.getTopVehiclesByYear(year, 5);
    }

    // ─── Top 5 xe - THÁNG ────────────────────────────────
    public List<VehicleReportRow> getTopVehiclesByMonth(int year, int month) {
        checkAdmin();
        return reportDAO.getTopVehiclesByMonth(year, month, 5);
    }

    // ─── Top 5 NV - NĂM ──────────────────────────────────
    public List<StaffReportRow> getTopStaffByYear(int year) {
        checkAdmin();
        return reportDAO.getTopStaffByYear(year, 5);
    }

    // ─── Top 5 NV - THÁNG ────────────────────────────────
    public List<StaffReportRow> getTopStaffByMonth(int year, int month) {
        checkAdmin();
        return reportDAO.getTopStaffByMonth(year, month, 5);
    }

    // ─── Thống kê xe theo trạng thái ─────────────────────
    public Map<String, Integer> getVehicleStatusCount() {
        checkAdmin();
        return reportDAO.getVehicleStatusCount();
    }

    // ─── Tháng trước (helper) ─────────────────────────────
    public double getPrevMonthRevenue(int year, int month) {
        checkAdmin();
        int py = month == 1 ? year - 1 : year;
        int pm = month == 1 ? 12 : month - 1;
        return reportDAO.getRevenueByMonth(py, pm);
    }

    public int getPrevMonthContracts(int year, int month) {
        checkAdmin();
        int py = month == 1 ? year - 1 : year;
        int pm = month == 1 ? 12 : month - 1;
        return reportDAO.getContractsByMonth(py, pm);
    }

    // ─── Format tiền ─────────────────────────────────────
    public static String formatMoneySmart(double amount) {
        if (amount >= 1_000_000_000)
            return String.format("%.1f tỷ", amount / 1_000_000_000).replace(".0 ", " ");
        if (amount >= 1_000_000)
            return String.format("%.1f tr", amount / 1_000_000).replace(".0 ", " ");
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

    /**
     * Lấy số lượng hợp đồng mà một nhân viên cụ thể đã thực hiện trong tháng hiện tại.
     * @param userId ID của nhân viên (từ bảng users)
     * @return Số lượng hợp đồng (không tính các hợp đồng đã hủy)
     */
    public int getStaffPerformanceCount(int userId) {
        if (userId <= 0) return 0;

        return reportDAO.getContractsByStaffThisMonth(userId);
    }
}