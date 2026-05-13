package com.example.rentalcar.dao;

import com.example.rentalcar.models.Customers;
import com.example.rentalcar.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO implements IBaseDAO<Customers, Integer>
{
    private Customers mapResultSetToCustomer(ResultSet rs) throws SQLException
    {
        Customers customer = new Customers();
        customer.setId_customer(rs.getInt("id_customer"));
        customer.setCccd(rs.getString("cccd"));
        customer.setFull_name(rs.getString("full_name"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        customer.setEmail(rs.getString("email"));
        customer.setCccd_images(rs.getString("cccd_images"));
        customer.setTrust_score(rs.getInt("trust_score"));
        customer.setIs_blacklist(rs.getBoolean("is_blacklist"));
        customer.setBlacklist_reason(rs.getString("blacklist_reason"));
        customer.setRental_count(rs.getInt("rental_count"));
        return customer;
    }

    // ==========================================================
    // ⭐ NEW: Tăng rental_count sau khi tạo hợp đồng thành công
    // ==========================================================
    public boolean incrementRentalCount(int customerId) {
        String sql = "UPDATE customers SET rental_count = rental_count + 1 WHERE id_customer = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, customerId);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI Tăng rental_count: " + e.getMessage());
            return false;
        }
    }

    // ==========================================================
    // PHẦN 1: HÀM ĐẶC THÙ
    // ==========================================================
    public Customers findByCccd(String cccd)
    {
        String sql = "SELECT * FROM Customers WHERE cccd = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, cccd);
            try (ResultSet rs = pstm.executeQuery())
            {
                if (rs.next()) return mapResultSetToCustomer(rs);
            }
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Tra cứu Khách hàng theo CCCD: " + e.getMessage());
        }
        return null;
    }

    // ==========================================================
    // PHẦN 2: CÁC HÀM IMPLEMENTS TỪ IBaseDAO
    // ==========================================================
    @Override
    public boolean insert(Customers entity)
    {
        int newId = 1;
        String sqlGetMaxId = "SELECT MAX(id_customer) FROM customers";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstmMax = cnt.prepareStatement(sqlGetMaxId);
             ResultSet rs = pstmMax.executeQuery()) {
            if (rs.next()) newId = rs.getInt(1) + 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql = "INSERT INTO customers (id_customer, cccd, full_name, phone, address, email, cccd_images, trust_score, is_blacklist, blacklist_reason) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 100, 0, ?)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, newId);
            pstm.setString(2, entity.getCccd());
            pstm.setString(3, entity.getFull_name());
            pstm.setString(4, entity.getPhone());
            pstm.setString(5, entity.getAddress());
            pstm.setString(6, entity.getEmail());
            pstm.setString(7, entity.getCccd_images());
            pstm.setString(8, entity.getBlacklist_reason());
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Thêm Khách hàng mới: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Customers entity)
    {
        String sql = "UPDATE Customers SET cccd = ?, full_name = ?, phone = ?, address = ?, email = ?, cccd_images = ?, trust_score = ?, is_blacklist = ?, blacklist_reason = ? WHERE id_customer = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getCccd());
            pstm.setString(2, entity.getFull_name());
            pstm.setString(3, entity.getPhone());
            pstm.setString(4, entity.getAddress());
            pstm.setString(5, entity.getEmail());
            pstm.setString(6, entity.getCccd_images());
            pstm.setInt(7, entity.getTrust_score());
            pstm.setBoolean(8, entity.isIs_blacklist());
            pstm.setString(9, entity.getBlacklist_reason());
            pstm.setInt(10, entity.getId_customer());
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Cập nhật thông tin Khách hàng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Integer id)
    {
        String sql = "DELETE FROM Customers WHERE id = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Xóa Khách hàng: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Customers findById(Integer id)
    {
        String sql = "SELECT * FROM Customers WHERE id_customer = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery())
            {
                if (rs.next()) return mapResultSetToCustomer(rs);
            }
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Tìm Khách hàng theo ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Customers> findAll()
    {
        List<Customers> listCustomers = new ArrayList<>();
        String sql = "SELECT * FROM Customers";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery())
        {
            while (rs.next()) listCustomers.add(mapResultSetToCustomer(rs));
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Lấy danh sách Khách hàng: " + e.getMessage());
        }
        return listCustomers;
    }
}