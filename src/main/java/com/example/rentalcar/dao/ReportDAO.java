package com.example.rentalcar.dao;

import com.example.rentalcar.models.StaffReportRow;
import com.example.rentalcar.models.VehicleReportRow;
import com.example.rentalcar.utils.DBConnection;

import java.sql.*;
import java.util.*;

public class ReportDAO {

    // =========================================================
    // 1. DOANH THU THEO THÁNG
    // =========================================================
    public Map<Integer, Double> getMonthlyRevenue(int year) {
        Map<Integer, Double> map = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) map.put(m, 0.0);

        String sql = "SELECT MONTH(start_datetime) AS month, " +
                "       SUM(total_price) AS revenue " +
                "FROM contracts " +
                "WHERE YEAR(start_datetime) = ? " +
                "  AND status NOT IN ('DA_HUY', 'DA HUY') " +
                "GROUP BY MONTH(start_datetime)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next())
                    map.put(rs.getInt("month"), rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getMonthlyRevenue lỗi: " + e.getMessage());
        }
        return map;
    }

    // =========================================================
    // 2. TỔNG DOANH THU NĂM
    // =========================================================
    public double getTotalRevenueByYear(int year) {
        String sql = "SELECT COALESCE(SUM(total_price), 0) " +
                "FROM contracts " +
                "WHERE YEAR(start_datetime) = ? " +
                "  AND status NOT IN ('DA_HUY', 'DA HUY')";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getTotalRevenueByYear lỗi: " + e.getMessage());
        }
        return 0;
    }

    // =========================================================
    // 3. TỔNG HỢP ĐỒNG HOÀN THÀNH
    // =========================================================
    public int getTotalContractsByYear(int year) {
        String sql = "SELECT COUNT(*) FROM contracts " +
                "WHERE YEAR(start_datetime) = ? " +
                "  AND status IN ('HOAN_THANH', 'HOAN THANH')";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getTotalContractsByYear lỗi: " + e.getMessage());
        }
        return 0;
    }

    // =========================================================
    // 4. TỔNG KHÁCH HÀNG THUÊ
    // =========================================================
    public int getTotalCustomersByYear(int year) {
        String sql = "SELECT COUNT(DISTINCT id_customer) FROM contracts " +
                "WHERE YEAR(start_datetime) = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getTotalCustomersByYear lỗi: " + e.getMessage());
        }
        return 0;
    }

    // =========================================================
    // 5. PHÂN BỐ TRẠNG THÁI HỢP ĐỒNG
    // =========================================================
    public Map<String, Integer> getContractStatusDistribution(int year) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS cnt FROM contracts " +
                "WHERE YEAR(start_datetime) = ? " +
                "GROUP BY status";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next())
                    map.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getContractStatusDistribution lỗi: " + e.getMessage());
        }
        return map;
    }

    // =========================================================
    // 6. TOP XE CHO THUÊ NHIỀU NHẤT - SỬA LẠI QUERY ĐƠN GIẢN HỚN
    // =========================================================
    public List<VehicleReportRow> getTopVehicles(int year, int limit) {
        List<VehicleReportRow> list = new ArrayList<>();

        // Dùng JOIN trực tiếp + GROUP BY thay vì subquery để tránh lỗi
        String sql =
                "SELECT v.brand, v.model, v.code_vehicle, " +
                        "       COUNT(c.id_contract) AS rental_count, " +
                        "       COALESCE(SUM(c.total_price), 0) AS total_revenue " +
                        "FROM contracts c " +
                        "JOIN vehicles v ON v.id_vehicle = c.id_vehicle " +
                        "WHERE YEAR(c.start_datetime) = ? " +
                        "  AND c.status NOT IN ('DA_HUY', 'DA HUY') " +
                        "  AND c.id_vehicle IS NOT NULL " +
                        "GROUP BY c.id_vehicle, v.brand, v.model, v.code_vehicle " +
                        "ORDER BY rental_count DESC " +
                        "LIMIT ?";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            pstm.setInt(2, limit);
            try (ResultSet rs = pstm.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    String name  = rs.getString("brand") + " " + rs.getString("model");
                    String plate = rs.getString("code_vehicle");
                    int    count = rs.getInt("rental_count");
                    double rev   = rs.getDouble("total_revenue");
                    list.add(new VehicleReportRow(rank++, name, plate, count, rev));
                }
            }
            System.out.println("[ReportDAO] getTopVehicles: " + list.size() + " kết quả, năm " + year);
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getTopVehicles lỗi: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================
    // 7. TOP NHÂN VIÊN THEO DOANH THU - SỬA LẠI QUERY ĐƠN GIẢN HỚN
    // =========================================================
    public List<StaffReportRow> getTopStaff(int year, int limit) {
        List<StaffReportRow> list = new ArrayList<>();

        // Dùng JOIN trực tiếp + GROUP BY thay vì subquery
        String sql =
                "SELECT u.full_name, " +
                        "       COUNT(c.id_contract) AS contract_count, " +
                        "       COALESCE(SUM(c.total_price), 0) AS total_revenue " +
                        "FROM contracts c " +
                        "JOIN users u ON u.id_user = c.id_user " +
                        "WHERE YEAR(c.start_datetime) = ? " +
                        "  AND c.status NOT IN ('DA_HUY', 'DA HUY') " +
                        "  AND c.id_user IS NOT NULL " +
                        "GROUP BY c.id_user, u.full_name " +
                        "ORDER BY total_revenue DESC " +
                        "LIMIT ?";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            pstm.setInt(2, limit);
            try (ResultSet rs = pstm.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    list.add(new StaffReportRow(
                            rank++,
                            rs.getString("full_name"),
                            rs.getInt("contract_count"),
                            rs.getDouble("total_revenue")));
                }
            }
            System.out.println("[ReportDAO] getTopStaff: " + list.size() + " kết quả, năm " + year);
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getTopStaff lỗi: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}