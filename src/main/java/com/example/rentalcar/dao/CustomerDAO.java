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
    // Hàm phụ trợ giúp map dữ liệu từ MySQL ResultSet ra đối tượng Customers (Chuẩn DRY)
    private Customers mapResultSetToCustomer(ResultSet rs) throws SQLException
    {
        Customers customer = new Customers();
        customer.setId_customer(rs.getInt("id"));
        customer.setCccd(rs.getString("cccd"));
        customer.setFull_name(rs.getString("full_name"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        customer.setCccd(rs.getString("cccd_images"));
        customer.setTrust_score(rs.getInt("trust_score"));
        customer.setIs_blacklist(rs.getBoolean("is_blacklisted"));
        customer.setBlacklist_reason(rs.getString("blacklist_reason"));

        return customer;
    }

    // ==========================================================
    // PHẦN 1: HÀM ĐẶC THÙ (Nghiệp vụ Tra cứu & Check rủi ro)
    // ==========================================================

    public Customers findByCccd(String cccd)
    {
        // Phục vụ Bước 1 của Staff: Nhập CCCD để xem khách cũ hay mới, có bị Blacklist không
        String sql = "SELECT * FROM Customers WHERE cccd = ?";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, cccd);

            try (ResultSet rs = pstm.executeQuery())
            {
                if (rs.next())
                {
                    return mapResultSetToCustomer(rs);
                }
            }
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Tra cứu Khách hàng theo CCCD: " + e.getMessage());
        }
        return null; // Trả về null nghĩa là Khách mới, yêu cầu Staff nhập form tạo mới
    }

    // ==========================================================
    // PHẦN 2: CÁC HÀM IMPLEMENTS TỪ IBaseDAO (Chuẩn CRUD)
    // ==========================================================

    @Override
    public boolean insert(Customers entity)
    {
        // Khi tạo khách mới, mặc định điểm tin cậy (trust_score) là 100 và không bị Blacklist (0)
        String sql = "INSERT INTO Customers (cccd, full_name, phone, address, cccd_images, trust_score, is_blacklisted, blacklist_reason) " +
                "VALUES (?, ?, ?, ?, ?, 100, 0, ?)";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getCccd());
            pstm.setString(2, entity.getFull_name());
            pstm.setString(3, entity.getPhone());
            pstm.setString(4, entity.getAddress());
            pstm.setString(5, entity.getCccd()); // Lưu đường dẫn (URL) ảnh chụp CCCD
            pstm.setString(6, entity.getBlacklist_reason()); // Thường là rỗng khi mới tạo

            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Thêm Khách hàng mới: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Customers entity)
    {
        // Cập nhật thông tin khách. Có thể dùng để Admin đưa khách vào danh sách đen (is_blacklisted = 1)
        String sql = "UPDATE Customers SET cccd = ?, full_name = ?, phone = ?, address = ?, cccd_images = ?, trust_score = ?, is_blacklisted = ?, blacklist_reason = ? WHERE id = ?";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getCccd());
            pstm.setString(2, entity.getFull_name());
            pstm.setString(3, entity.getPhone());
            pstm.setString(4, entity.getAddress());
            pstm.setString(5, entity.getCccd());
            pstm.setInt(6, entity.getTrust_score());
            pstm.setBoolean(7, entity.isIs_blacklist());
            pstm.setString(8, entity.getBlacklist_reason());
            pstm.setInt(9, entity.getId_customer());

            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Cập nhật thông tin Khách hàng: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Integer id)
    {
        // LƯU Ý NGHIỆP VỤ: Trong thực tế, dữ liệu khách hàng liên kết với rất nhiều Hợp đồng (Khóa ngoại).
        // Xóa cứng (DELETE) sẽ bị lỗi Ràng buộc toàn vẹn (Constraint Violation).
        // Thay vì xóa, ở đây ta sẽ mô phỏng việc "Xóa" bằng cách đưa khách vào Blacklist vĩnh viễn, hoặc ẩn đi.
        // Ở đây anh dùng lệnh DELETE thuần để em biết cách bắt lỗi, nhưng nếu chạy thực tế sinh lỗi thì nên chuyển sang UPDATE.

        String sql = "DELETE FROM Customers WHERE id = ?";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Xóa Khách hàng (Có thể do vướng Khóa ngoại ở bảng Contracts): " + e.getMessage());
            return false;
        }
    }

    @Override
    public Customers findById(Integer id)
    {
        String sql = "SELECT * FROM Customers WHERE id = ?";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);

            try (ResultSet rs = pstm.executeQuery())
            {
                if (rs.next())
                {
                    return mapResultSetToCustomer(rs);
                }
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
            while (rs.next())
            {
                listCustomers.add(mapResultSetToCustomer(rs));
            }
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Lấy danh sách Khách hàng: " + e.getMessage());
        }
        return listCustomers;
    }
}