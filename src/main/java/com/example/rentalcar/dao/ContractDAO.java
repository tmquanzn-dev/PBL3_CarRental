package com.example.rentalcar.dao;

import com.example.rentalcar.models.*;
import com.example.rentalcar.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContractDAO implements IBaseDAO<Contracts, Integer> {

    // ==========================================================
    // HÀM PHỤ TRỢ: Map dữ liệu từ MySQL ra Java Object
    // ==========================================================
    private Contracts mapResultSetToContract(ResultSet rs) throws SQLException {
        Contracts contract = new Contracts();
        contract.setId_contract(rs.getInt("id_contract"));
        contract.setCode_contract(rs.getString("code_contract"));

        if (rs.getTimestamp("start_datetime") != null)
            contract.setStart_datetime(rs.getTimestamp("start_datetime").toLocalDateTime());
        if (rs.getTimestamp("end_datetime") != null)
            contract.setEnd_datetime(rs.getTimestamp("end_datetime").toLocalDateTime());
        if (rs.getTimestamp("return_datetime") != null)
            contract.setReturn_datetime(rs.getTimestamp("return_datetime").toLocalDateTime());

        contract.setKm_start(rs.getInt("km_start"));
        contract.setKm_end(rs.getInt("km_end"));
        contract.setFuel_start(rs.getInt("fuel_start"));
        contract.setFuel_end(rs.getInt("fuel_end"));
        contract.setDeposit_amount(rs.getDouble("deposit_amount"));
        contract.setBase_price(rs.getDouble("base_price"));
        contract.setDiscount_amount(rs.getDouble("discount_amount"));
        contract.setTotal_price(rs.getDouble("total_price"));

        if (rs.getString("deposit_type") != null)
            contract.setDeposit_type(DepositType.valueOf(rs.getString("deposit_type").replace(" ", "_")));
        if (rs.getString("payment_status") != null)
            contract.setPayment_status(PaymentStatus.valueOf(rs.getString("payment_status").replace(" ", "_")));
        if (rs.getString("status") != null)
            contract.setStatus(StatusContracts.valueOf(rs.getString("status").replace(" ", "_")));

        // Nạp đầy đủ thông tin nhân viên (Tránh lỗi hiển thị nhân viên lập)
        Users user = new Users();
        user.setId_user(rs.getInt("id_user"));
        if (hasColumn(rs, "user_full_name")) {
            user.setFull_name(rs.getString("user_full_name"));
        }
        contract.setId_user(user);

        // Nạp đầy đủ thông tin xe máy
        Vehicles vehicle = new Vehicles();
        vehicle.setId_vehicle(rs.getInt("id_vehicle"));
        if (hasColumn(rs, "vehicle_code")) {
            vehicle.setCode_vehicle(rs.getString("vehicle_code"));
            vehicle.setBrand(rs.getString("vehicle_brand"));
            vehicle.setModel(rs.getString("vehicle_model"));
        }
        contract.setId_vehicle(vehicle);

        // Nạp đầy đủ thông tin khách hàng
        Customers customer = new Customers();
        customer.setId_customer(rs.getInt("id_customer"));
        if (hasColumn(rs, "customer_full_name")) {
            customer.setFull_name(rs.getString("customer_full_name"));
            customer.setPhone(rs.getString("customer_phone"));
        }
        contract.setId_customer(customer);

        int idVoucher = rs.getInt("id_voucher");
        if (!rs.wasNull()) {
            Vouchers v = new Vouchers();
            v.setId_voucher(idVoucher);
            if (hasColumn(rs, "voucher_code")) {
                v.setCode_vouchers(rs.getString("voucher_code"));
            }
            contract.setId_voucher(v);
        }
        return contract;
    }

    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // ✅ ĐÃ FIX: Sửa thành vc.code_voucher để khớp chuẩn xác database
    private final String SELECT_BASE =
            "SELECT c.*, " +
                    "u.full_name AS user_full_name, " +
                    "v.code_vehicle AS vehicle_code, v.brand AS vehicle_brand, v.model AS vehicle_model, " +
                    "cust.full_name AS customer_full_name, cust.phone AS customer_phone, " +
                    "vc.code_voucher AS voucher_code " +
                    "FROM Contracts c " +
                    "LEFT JOIN users u ON c.id_user = u.id_user " +
                    "LEFT JOIN vehicles v ON c.id_vehicle = v.id_vehicle " +
                    "LEFT JOIN customers cust ON c.id_customer = cust.id_customer " +
                    "LEFT JOIN vouchers vc ON c.id_voucher = vc.id_voucher ";

    @Override
    public boolean insert(Contracts entity) {
        String sql = "INSERT INTO Contracts (code_contract, start_datetime, end_datetime, km_start, fuel_start, " +
                "deposit_type, deposit_amount, base_price, discount_amount, total_price, payment_status, status, " +
                "id_user, id_vehicle, id_customer, id_voucher) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {

            pstm.setString(1, entity.getCode_contract());
            pstm.setTimestamp(2, Timestamp.valueOf(entity.getStart_datetime()));
            pstm.setTimestamp(3, Timestamp.valueOf(entity.getEnd_datetime()));
            pstm.setInt(4, entity.getKm_start());
            pstm.setInt(5, entity.getFuel_start());
            pstm.setString(6, entity.getDeposit_type().name().replace("_", " "));
            pstm.setDouble(7, entity.getDeposit_amount());
            pstm.setDouble(8, entity.getBase_price());
            pstm.setDouble(9, entity.getDiscount_amount());
            pstm.setDouble(10, entity.getTotal_price());
            pstm.setString(11, entity.getPayment_status().name().replace("_", " "));
            pstm.setString(12, entity.getStatus().name().replace("_", " "));
            pstm.setInt(13, entity.getId_user().getId_user());
            pstm.setInt(14, entity.getId_vehicle().getId_vehicle());
            pstm.setInt(15, entity.getId_customer().getId_customer());

            if (entity.getId_voucher() != null && entity.getId_voucher().getId_voucher() > 0)
                pstm.setInt(16, entity.getId_voucher().getId_voucher());
            else
                pstm.setNull(16, Types.INTEGER);

            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Contracts entity) {
        // ĐÃ VÁ LỖI: Bổ sung thêm base_price = ? vào câu lệnh UPDATE để MySQL ghi nhận giá gốc mới
        String sql = "UPDATE Contracts SET end_datetime = ?, return_datetime = ?, km_end = ?, fuel_end = ?, " +
                "base_price = ?, discount_amount = ?, total_price = ?, payment_status = ?, status = ? WHERE id_contract = ?";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {

            pstm.setTimestamp(1, Timestamp.valueOf(entity.getEnd_datetime()));
            if (entity.getReturn_datetime() != null)
                pstm.setTimestamp(2, Timestamp.valueOf(entity.getReturn_datetime()));
            else
                pstm.setNull(2, Types.TIMESTAMP);

            pstm.setInt(3, entity.getKm_end());
            pstm.setInt(4, entity.getFuel_end());
            pstm.setDouble(5, entity.getBase_price());       // Thêm nạp dữ liệu base_price vị trí số 5
            pstm.setDouble(6, entity.getDiscount_amount());   // Dịch chuyển vị trí lên số 6
            pstm.setDouble(7, entity.getTotal_price());       // Dịch chuyển vị trí lên số 7
            pstm.setString(8, entity.getPayment_status().name().replace("_", " ")); // Vị trí số 8
            pstm.setString(9, entity.getStatus().name().replace("_", " "));         // Vị trí số 9
            pstm.setInt(10, entity.getId_contract());                               // Vị trí số 10

            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI UPDATE CONTRACT DAO: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean delete(Integer id) {
        String sql = "UPDATE Contracts SET status = 'DA HUY' WHERE id_contract = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Contracts findById(Integer id) {
        String sql = SELECT_BASE + "WHERE c.id_contract = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapResultSetToContract(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Contracts> findAll() {
        List<Contracts> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY c.id_contract DESC";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             Statement st = cnt.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSetToContract(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Contracts findByCode(String code) {
        String sql = SELECT_BASE + "WHERE c.code_contract = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setString(1, code);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapResultSetToContract(rs);
            }
        } catch (SQLException e) {
            System.err.println("LỖI findByCode: " + e.getMessage());
        }
        return null;
    }

    public boolean isCodeExists(String code) {
        String sql = "SELECT COUNT(*) FROM Contracts WHERE code_contract = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setString(1, code);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("LỖI isCodeExists: " + e.getMessage());
        }
        return false;
    }

    public int markOverdueContracts() {
        String sql = "UPDATE Contracts SET status = 'QUA HAN' " +
                "WHERE status = 'DANG THUE' AND end_datetime < NOW()";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             Statement st = cnt.createStatement()) {
            return st.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("LỖI markOverdueContracts: " + e.getMessage());
            return 0;
        }
    }

    public boolean deleteAndDecrementRentalCount(int contractId) {
        Contracts contract = findById(contractId);
        if (contract == null) return false;

        String sqlCancel = "UPDATE Contracts SET status = 'DA HUY' WHERE id_contract = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sqlCancel)) {
            pstm.setInt(1, contractId);
            boolean cancelled = pstm.executeUpdate() > 0;

            if (cancelled && contract.getId_customer() != null) {
                String sqlDecrement = "UPDATE customers SET rental_count = GREATEST(rental_count - 1, 0) " +
                        "WHERE id_customer = ?";
                try (PreparedStatement pstm2 = cnt.prepareStatement(sqlDecrement)) {
                    pstm2.setInt(1, contract.getId_customer().getId_customer());
                    pstm2.executeUpdate();
                }
            }
            return cancelled;
        } catch (SQLException e) {
            System.err.println("LỖI deleteAndDecrementRentalCount: " + e.getMessage());
            return false;
        }
    }

    // ==========================================================
    // CÁC HÀM THỐNG KÊ
    // ==========================================================
    public double getMonthlyRevenue() {
        String sql = "SELECT SUM(total_price) FROM Contracts WHERE MONTH(start_datetime) = MONTH(CURDATE()) " +
                "AND YEAR(start_datetime) = YEAR(CURDATE()) AND status != 'DA HUY'";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             Statement st = cnt.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public double getTodayRevenue() {
        String sql = "SELECT SUM(total_price) FROM Contracts WHERE DATE(start_datetime) = CURDATE() AND status != 'DA HUY'";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             Statement st = cnt.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getActiveContractsCount() {
        String sql = "SELECT COUNT(*) FROM Contracts WHERE status = 'DANG THUE'";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             Statement st = cnt.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<Contracts> findRecentContracts(int limit) {
        List<Contracts> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY c.id_contract DESC LIMIT ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, limit);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToContract(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}