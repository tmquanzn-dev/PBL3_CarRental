package com.example.rentalcar.dao;

import com.example.rentalcar.models.*;
import com.example.rentalcar.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO implements IBaseDAO<Payments, Integer> {

    private Payments mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payments p = new Payments();
        p.setId_payment(rs.getInt("id_payment"));
        p.setAmount(rs.getDouble("amount"));

        // Ép kiểu an toàn Enum Loại thanh toán tránh xung đột chuỗi chữ hoa/thường hoặc khoảng trắng từ DB
        String typeStr = rs.getString("payment_type");
        if (typeStr != null) {
            try {
                p.setPayment_type(PaymentType.valueOf(typeStr.toUpperCase().replace(" ", "_")));
            } catch (IllegalArgumentException e) {
                p.setPayment_type(PaymentType.THANH_TOAN_PHAN_CON_LAI); // fallback an toàn
            }
        }

        // Ép kiểu an toàn Enum Phương thức thanh toán
        String methodStr = rs.getString("payment_method");
        if (methodStr != null) {
            try {
                p.setPayment_method(PaymentMethod.valueOf(methodStr.toUpperCase().replace(" ", "_")));
            } catch (IllegalArgumentException e) {
                p.setPayment_method(PaymentMethod.TIEN_MAT); // fallback an toàn
            }
        }

        // Khởi tạo đối tượng User và Contract "vỏ" (chỉ chứa ID) để liên kết thực thể dữ liệu
        Users user = new Users();
        user.setId_user(rs.getInt("id_user"));
        p.setId_user(user);

        Contracts contract = new Contracts();
        contract.setId_contract(rs.getInt("id_contract"));
        p.setId_contract(contract);

        return p;
    }

    /**
     * Tìm kiếm toàn bộ danh sách lịch sử nạp tiền của một hợp đồng chỉ định
     */
    public List<Payments> findByContractId(int contractId) {
        List<Payments> list = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE id_contract = ? ORDER BY id_payment ASC";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {

            pstm.setInt(1, contractId);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("LỖI findByContractId: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean insert(Payments entity) {
        String sql = "INSERT INTO payments (amount, payment_type, payment_method, id_user, id_contract) VALUES (?, ?, ?, ?, ?)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setDouble(1, entity.getAmount());
            pstm.setString(2, entity.getPayment_type().name().replace("_", " "));
            pstm.setString(3, entity.getPayment_method().name().replace("_", " "));
            pstm.setInt(4, entity.getId_user().getId_user());
            pstm.setInt(5, entity.getId_contract().getId_contract());
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI Thêm Payment: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Payments entity) {
        String sql = "UPDATE payments SET amount = ?, payment_type = ?, payment_method = ?, id_user = ?, id_contract = ? WHERE id_payment = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setDouble(1, entity.getAmount());
            pstm.setString(2, entity.getPayment_type().name().replace("_", " "));
            pstm.setString(3, entity.getPayment_method().name().replace("_", " "));
            pstm.setInt(4, entity.getId_user().getId_user());
            pstm.setInt(5, entity.getId_contract().getId_contract());
            pstm.setInt(6, entity.getId_payment());
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI Cập nhật Payment: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM payments WHERE id_payment = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI Xóa Payment: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Payments findById(Integer id) {
        String sql = "SELECT * FROM payments WHERE id_payment = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapResultSetToPayment(rs);
            }
        } catch (SQLException e) {
            System.err.println("LỖI Tìm Payment: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Payments> findAll() {
        List<Payments> list = new ArrayList<>();
        String sql = "SELECT * FROM payments";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToPayment(rs));
        } catch (SQLException e) {
            System.err.println("LỖI Lấy DS Payment: " + e.getMessage());
        }
        return list;
    }
}