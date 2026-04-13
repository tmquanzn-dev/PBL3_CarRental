package com.example.rentalcar.dao;

import com.example.rentalcar.models.DiscountType;
import com.example.rentalcar.models.Vouchers;
import com.example.rentalcar.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoucherDAO implements IBaseDAO<Vouchers, Integer>
{
    // ==========================================================
    // HÀM MAP DỮ LIỆU TỪ MYSQL RA JAVA
    // ==========================================================
    private Vouchers mapResultSetToVoucher(ResultSet rs) throws SQLException {
        Vouchers v = new Vouchers();
        v.setId_voucher(rs.getInt("id_voucher"));

        // Chú ý: Cột DB là "code_voucher", nhưng hàm set của em là "code_vouchers"
        v.setCode_vouchers(rs.getString("code_voucher"));
        v.setDescription(rs.getString("description"));

        // Đọc Enum từ DB: Biến "CO DINH" thành "CO_DINH" để Java hiểu được
        String dbDiscountType = rs.getString("discount_type");
        if (dbDiscountType != null) {
            dbDiscountType = dbDiscountType.replace(" ", "_");
            v.setDiscount_type(DiscountType.valueOf(dbDiscountType));
        }

        v.setDiscount_value(rs.getDouble("discount_value"));
        v.setUsage_limit(rs.getInt("usage_limit"));
        v.setUsage_count(rs.getInt("usage_count"));

        // Vì Model của em dùng sẵn java.sql.Date nên chỉ cần set thẳng, không cần ép kiểu
        v.setValid_from_date(rs.getDate("valid_from_date"));
        v.setValid_to_date(rs.getDate("valid_to_date"));

        v.setIs_active(rs.getBoolean("is_active"));
        return v;
    }

    // ==========================================================
    // CÁC HÀM CRUD CƠ BẢN
    // ==========================================================
    @Override
    public boolean insert(Vouchers entity)
    {
        String sql = "INSERT INTO vouchers (code_voucher, description, discount_type, discount_value, usage_limit, usage_count, valid_from_date, valid_to_date, is_active) VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getCode_vouchers());
            pstm.setString(2, entity.getDescription());

            // Lưu Enum xuống DB: Biến "CO_DINH" lại thành "CO DINH" để lưu vào MySQL
            pstm.setString(3, entity.getDiscount_type().name().replace("_", " "));

            pstm.setDouble(4, entity.getDiscount_value());
            pstm.setInt(5, entity.getUsage_limit());

            // Lấy thẳng java.sql.Date từ Model nhét vào
            pstm.setDate(6, entity.getValid_from_date());
            pstm.setDate(7, entity.getValid_to_date());
            pstm.setBoolean(8, entity.isIs_active());

            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e) { System.err.println("LỖI Thêm Voucher: " + e.getMessage()); return false; }
    }

    @Override
    public boolean update(Vouchers entity)
    {
        String sql = "UPDATE vouchers SET code_voucher = ?, description = ?, discount_type = ?, discount_value = ?, usage_limit = ?, usage_count = ?, valid_from_date = ?, valid_to_date = ?, is_active = ? WHERE id_voucher = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getCode_vouchers());
            pstm.setString(2, entity.getDescription());
            pstm.setString(3, entity.getDiscount_type().name().replace("_", " "));
            pstm.setDouble(4, entity.getDiscount_value());
            pstm.setInt(5, entity.getUsage_limit());
            pstm.setInt(6, entity.getUsage_count());
            pstm.setDate(7, entity.getValid_from_date());
            pstm.setDate(8, entity.getValid_to_date());
            pstm.setBoolean(9, entity.isIs_active());
            pstm.setInt(10, entity.getId_voucher());
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e) { System.err.println("LỖI Cập nhật Voucher: " + e.getMessage()); return false; }
    }

    @Override
    public boolean delete(Integer id)
    {
        // Xóa Mềm: Tắt trạng thái để không áp dụng vào Hợp đồng mới nữa
        String sql = "UPDATE vouchers SET is_active = 0 WHERE id_voucher = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e) { System.err.println("LỖI Khóa Voucher: " + e.getMessage()); return false; }
    }

    @Override
    public Vouchers findById(Integer id)
    {
        String sql = "SELECT * FROM vouchers WHERE id_voucher = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                // Trả về 1 kết quả duy nhất -> Dùng if
                if (rs.next()) return mapResultSetToVoucher(rs);
            }
        }
        catch (SQLException e) { System.err.println("LỖI Tìm Voucher: " + e.getMessage()); }
        return null;
    }

    @Override
    public List<Vouchers> findAll()
    {
        List<Vouchers> list = new ArrayList<>();
        String sql = "SELECT * FROM vouchers ORDER BY id_voucher DESC";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery())
        {
            // Trả về nhiều kết quả -> Dùng while
            while (rs.next()) list.add(mapResultSetToVoucher(rs));
        }
        catch (SQLException e) { System.err.println("LỖI Lấy DS Voucher: " + e.getMessage()); }
        return list;
    }

    // ==========================================================
    // HÀM NGHIỆP VỤ ĐẶC THÙ (Dùng khi khách hàng nhập mã KM)
    // ==========================================================
    public Vouchers findByCode(String code)
    {
        // Điều kiện: Mã phải khớp, Đang kích hoạt (is_active = 1), và Vẫn còn hạn (>= CURDATE())
        String sql = "SELECT * FROM vouchers WHERE code_voucher = ? AND is_active = 1 AND valid_to_date >= CURDATE()";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, code);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapResultSetToVoucher(rs);
            }
        }
        catch (SQLException e) { System.err.println("LỖI Tìm Voucher theo Code: " + e.getMessage()); }
        return null;
    }
}