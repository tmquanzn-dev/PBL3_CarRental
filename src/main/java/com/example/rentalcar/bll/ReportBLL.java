package com.example.rentalcar.bll;

import com.example.rentalcar.dao.ReportDAO;
import com.example.rentalcar.models.StaffReportRow;
import com.example.rentalcar.models.VehicleReportRow;
import com.example.rentalcar.utils.AppSession;

import java.util.List;
import java.util.Map;

public class ReportBLL {

    private final ReportDAO reportDAO = new ReportDAO();

    // -------------------------------------------------------
    // Doanh thu theo tháng (Map: tháng 1..12 → VNĐ)
    // -------------------------------------------------------
    public Map<Integer, Double> getMonthlyRevenue(int year) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo: Dữ liệu báo cáo chỉ dành cho Quản trị viên!"); //
        return reportDAO.getMonthlyRevenue(year);
    }

    // -------------------------------------------------------
    // Tổng doanh thu năm
    // -------------------------------------------------------
    public double getTotalRevenue(int year) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo: Dữ liệu báo cáo chỉ dành cho Quản trị viên!"); //
        return reportDAO.getTotalRevenueByYear(year);
    }

    // -------------------------------------------------------
    // % tăng trưởng doanh thu so với năm trước
    //   > 0 : tăng,  < 0 : giảm,  0 : không đổi / không có dữ liệu
    // -------------------------------------------------------
    public double getRevenueGrowthPercent(int year) {
        double current = reportDAO.getTotalRevenueByYear(year);
        double prev    = reportDAO.getTotalRevenueByYear(year - 1);
        if (prev <= 0) return 0;
        return ((current - prev) / prev) * 100.0;
    }

    // -------------------------------------------------------
    // Tổng hợp đồng hoàn thành
    // -------------------------------------------------------
    public int getTotalContracts(int year) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo: Dữ liệu báo cáo chỉ dành cho Quản trị viên!"); //
        return reportDAO.getTotalContractsByYear(year);
    }

    // -------------------------------------------------------
    // Tổng khách hàng thuê (distinct)
    // -------------------------------------------------------
    public int getTotalCustomers(int year) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo: Dữ liệu báo cáo chỉ dành cho Quản trị viên!"); //
        return reportDAO.getTotalCustomersByYear(year);
    }

    // -------------------------------------------------------
    // Doanh thu trung bình / tháng
    // -------------------------------------------------------
    public double getAvgMonthlyRevenue(int year) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo: Dữ liệu báo cáo chỉ dành cho Quản trị viên!"); //
        return getTotalRevenue(year) / 12.0;
    }

    // -------------------------------------------------------
    // Phân bố trạng thái hợp đồng
    // -------------------------------------------------------
    public Map<String, Integer> getContractStatusDistribution(int year) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo: Dữ liệu báo cáo chỉ dành cho Quản trị viên!"); //
        return reportDAO.getContractStatusDistribution(year);
    }

    // -------------------------------------------------------
    // Top 5 xe cho thuê nhiều nhất
    // -------------------------------------------------------
    public List<VehicleReportRow> getTopVehicles(int year) {
        return reportDAO.getTopVehicles(year, 5);
    }

    // -------------------------------------------------------
    // Top 5 nhân viên theo doanh thu
    // -------------------------------------------------------
    public List<StaffReportRow> getTopStaff(int year) {
        if (!AppSession.isAdmin()) throw new IllegalStateException("Cảnh báo: Dữ liệu báo cáo chỉ dành cho Quản trị viên!"); //
        return reportDAO.getTopStaff(year, 5);
    }

    // -------------------------------------------------------
    // Helper: format tiền VNĐ gọn
    //   1.500.000  →  "1,5 tr"
    //   85.000.000 →  "85 tr"
    //   1.200.000.000 → "1,2 tỷ"
    // -------------------------------------------------------
    public static String formatMoneySmart(double amount) {
        if (amount >= 1_000_000_000)
            return String.format("%.1f tỷ", amount / 1_000_000_000).replace(".0 ", " ");
        if (amount >= 1_000_000)
            return String.format("%.1f tr", amount / 1_000_000).replace(".0 ", " ");
        return String.format("%,.0f đ", amount).replace(",", ".");
    }

    // Full format: 1.500.000 đ
    public static String formatMoneyFull(double amount) {
        return String.format("%,.0f đ", amount).replace(",", ".");
    }
}