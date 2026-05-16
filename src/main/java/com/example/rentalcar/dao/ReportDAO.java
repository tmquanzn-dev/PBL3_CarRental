package com.example.rentalcar.dao;

import com.example.rentalcar.models.StaffReportRow;
import com.example.rentalcar.models.VehicleReportRow;
import com.example.rentalcar.utils.DBConnection;

import java.sql.*;
import java.util.*;

public class ReportDAO {

    // =========================================================
    // 1. DOANH THU THEO THÁNG (cho BarChart)
    // =========================================================
    public Map<Integer, Double> getMonthlyRevenue(int year) {
        Map<Integer, Double> map = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) map.put(m, 0.0);

        String sql = "SELECT MONTH(start_datetime) AS month, " +
                "       COALESCE(SUM(total_price), 0) AS revenue " +
                "FROM contracts " +
                "WHERE YEAR(start_datetime) = ? " +
                "  AND status NOT IN ('DA_HUY','DA HUY') " +
                "GROUP BY MONTH(start_datetime)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next())
                    map.put(rs.getInt("month"), rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getMonthlyRevenue: " + e.getMessage());
        }
        return map;
    }

    // =========================================================
    // 2. TỔNG DOANH THU NĂM
    // =========================================================
    public double getTotalRevenueByYear(int year) {
        String sql = "SELECT COALESCE(SUM(total_price),0) FROM contracts " +
                "WHERE YEAR(start_datetime)=? AND status NOT IN ('DA_HUY','DA HUY')";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getTotalRevenueByYear: " + e.getMessage());
        }
        return 0;
    }

    // =========================================================
    // 3. DOANH THU THÁNG CỤ THỂ
    // =========================================================
    public double getRevenueByMonth(int year, int month) {
        String sql = "SELECT COALESCE(SUM(total_price),0) FROM contracts " +
                "WHERE YEAR(start_datetime)=? AND MONTH(start_datetime)=? " +
                "  AND status NOT IN ('DA_HUY','DA HUY')";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            pstm.setInt(2, month);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getRevenueByMonth: " + e.getMessage());
        }
        return 0;
    }

    // =========================================================
    // 4. SỐ HỢP ĐỒNG HOÀN THÀNH NĂM
    // =========================================================
    public int getTotalContractsByYear(int year) {
        String sql = "SELECT COUNT(*) FROM contracts " +
                "WHERE YEAR(start_datetime)=? AND status IN ('HOAN_THANH','HOAN THANH')";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getTotalContractsByYear: " + e.getMessage());
        }
        return 0;
    }

    // =========================================================
    // 5. SỐ HỢP ĐỒNG THÁNG CỤ THỂ
    // =========================================================
    public int getContractsByMonth(int year, int month) {
        String sql = "SELECT COUNT(*) FROM contracts " +
                "WHERE YEAR(start_datetime)=? AND MONTH(start_datetime)=? " +
                "  AND status NOT IN ('DA_HUY','DA HUY')";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            pstm.setInt(2, month);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getContractsByMonth: " + e.getMessage());
        }
        return 0;
    }

    // =========================================================
    // 6. SỐ KHÁCH HÀNG NĂM (distinct)
    // =========================================================
    public int getTotalCustomersByYear(int year) {
        String sql = "SELECT COUNT(DISTINCT id_customer) FROM contracts WHERE YEAR(start_datetime)=?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getTotalCustomersByYear: " + e.getMessage());
        }
        return 0;
    }

    // =========================================================
    // 7. SỐ KHÁCH HÀNG THÁNG CỤ THỂ
    // =========================================================
    public int getCustomersByMonth(int year, int month) {
        String sql = "SELECT COUNT(DISTINCT id_customer) FROM contracts " +
                "WHERE YEAR(start_datetime)=? AND MONTH(start_datetime)=?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            pstm.setInt(2, month);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getCustomersByMonth: " + e.getMessage());
        }
        return 0;
    }

    // =========================================================
    // 8. PHÂN BỐ TRẠNG THÁI HỢP ĐỒNG
    // =========================================================
    public Map<String, Integer> getContractStatusDistribution(int year) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS cnt FROM contracts " +
                "WHERE YEAR(start_datetime)=? GROUP BY status";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next())
                    map.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getContractStatusDistribution: " + e.getMessage());
        }
        return map;
    }

    // =========================================================
    // 9. TOP 5 XE THEO NĂM
    // =========================================================
    public List<VehicleReportRow> getTopVehiclesByYear(int year, int limit) {
        List<VehicleReportRow> list = new ArrayList<>();
        String sql = "SELECT v.brand, v.model, v.code_vehicle, " +
                "       COUNT(c.id_contract) AS rental_count, " +
                "       COALESCE(SUM(c.total_price),0) AS total_revenue " +
                "FROM contracts c " +
                "JOIN vehicles v ON v.id_vehicle = c.id_vehicle " +
                "WHERE YEAR(c.start_datetime)=? " +
                "  AND c.status NOT IN ('DA_HUY','DA HUY') " +
                "GROUP BY c.id_vehicle, v.brand, v.model, v.code_vehicle " +
                "ORDER BY rental_count DESC LIMIT ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            pstm.setInt(2, limit);
            try (ResultSet rs = pstm.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    list.add(new VehicleReportRow(rank++,
                            rs.getString("brand") + " " + rs.getString("model"),
                            rs.getString("code_vehicle"),
                            rs.getInt("rental_count"),
                            rs.getDouble("total_revenue")));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getTopVehiclesByYear: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // 10. TOP 5 XE THEO THÁNG CỤ THỂ ← MỚI
    // =========================================================
    public List<VehicleReportRow> getTopVehiclesByMonth(int year, int month, int limit) {
        List<VehicleReportRow> list = new ArrayList<>();
        String sql = "SELECT v.brand, v.model, v.code_vehicle, " +
                "       COUNT(c.id_contract) AS rental_count, " +
                "       COALESCE(SUM(c.total_price),0) AS total_revenue " +
                "FROM contracts c " +
                "JOIN vehicles v ON v.id_vehicle = c.id_vehicle " +
                "WHERE YEAR(c.start_datetime)=? AND MONTH(c.start_datetime)=? " +
                "  AND c.status NOT IN ('DA_HUY','DA HUY') " +
                "GROUP BY c.id_vehicle, v.brand, v.model, v.code_vehicle " +
                "ORDER BY rental_count DESC LIMIT ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            pstm.setInt(2, month);
            pstm.setInt(3, limit);
            try (ResultSet rs = pstm.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    list.add(new VehicleReportRow(rank++,
                            rs.getString("brand") + " " + rs.getString("model"),
                            rs.getString("code_vehicle"),
                            rs.getInt("rental_count"),
                            rs.getDouble("total_revenue")));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getTopVehiclesByMonth: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // 11. TOP 5 NHÂN VIÊN THEO NĂM
    // =========================================================
    public List<StaffReportRow> getTopStaffByYear(int year, int limit) {
        List<StaffReportRow> list = new ArrayList<>();
        String sql = "SELECT u.full_name, " +
                "       COUNT(c.id_contract) AS contract_count, " +
                "       COALESCE(SUM(c.total_price),0) AS total_revenue " +
                "FROM contracts c " +
                "JOIN users u ON u.id_user = c.id_user " +
                "WHERE YEAR(c.start_datetime)=? " +
                "  AND c.status NOT IN ('DA_HUY','DA HUY') " +
                "GROUP BY c.id_user, u.full_name " +
                "ORDER BY total_revenue DESC LIMIT ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            pstm.setInt(2, limit);
            try (ResultSet rs = pstm.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    list.add(new StaffReportRow(rank++,
                            rs.getString("full_name"),
                            rs.getInt("contract_count"),
                            rs.getDouble("total_revenue")));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getTopStaffByYear: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // 12. TOP 5 NHÂN VIÊN THEO THÁNG ← MỚI
    // =========================================================
    public List<StaffReportRow> getTopStaffByMonth(int year, int month, int limit) {
        List<StaffReportRow> list = new ArrayList<>();
        String sql = "SELECT u.full_name, " +
                "       COUNT(c.id_contract) AS contract_count, " +
                "       COALESCE(SUM(c.total_price),0) AS total_revenue " +
                "FROM contracts c " +
                "JOIN users u ON u.id_user = c.id_user " +
                "WHERE YEAR(c.start_datetime)=? AND MONTH(c.start_datetime)=? " +
                "  AND c.status NOT IN ('DA_HUY','DA HUY') " +
                "GROUP BY c.id_user, u.full_name " +
                "ORDER BY total_revenue DESC LIMIT ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, year);
            pstm.setInt(2, month);
            pstm.setInt(3, limit);
            try (ResultSet rs = pstm.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    list.add(new StaffReportRow(rank++,
                            rs.getString("full_name"),
                            rs.getInt("contract_count"),
                            rs.getDouble("total_revenue")));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getTopStaffByMonth: " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    // 13. THỐNG KÊ XE THEO TRẠNG THÁI
    // =========================================================
    public Map<String, Integer> getVehicleStatusCount() {
        Map<String, Integer> map = new HashMap<>();
        map.put("AVAILABLE", 0);
        map.put("RENTED", 0);
        map.put("MAINTENANCE", 0);
        map.put("TOTAL", 0);

        String sql = "SELECT status, COUNT(*) AS cnt FROM vehicles " +
                "WHERE status != 'INACTIVE' GROUP BY status";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            int total = 0;
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("cnt");
                map.put(status, count);
                total += count;
            }
            map.put("TOTAL", total);
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getVehicleStatusCount: " + e.getMessage());
        }
        return map;
    }

    public int getContractsByStaffThisMonth(int userId) {
        String sql = "SELECT COUNT(*) FROM contracts " +
                "WHERE id_user = ? " +
                "AND MONTH(start_datetime) = MONTH(CURDATE()) " +
                "AND YEAR(start_datetime) = YEAR(CURDATE()) " +
                "AND status != 'DA HUY'";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, userId);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("LỖI getContractsByStaff: " + e.getMessage());
        }
        return 0;
    }
}