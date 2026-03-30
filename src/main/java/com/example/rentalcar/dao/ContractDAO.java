package com.example.rentalcar.dao;

import com.example.rentalcar.models.*;
import com.example.rentalcar.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ContractDAO implements IBaseDAO<Contracts, Integer>
{
    // ==========================================================
    // HÀM PHỤ TRỢ: Map dữ liệu từ MySQL ra Java Object
    // ==========================================================
    private Contracts mapResultSetToContract(ResultSet rs) throws SQLException
    {
        Contracts contract = new Contracts();
        contract.setId_contract(rs.getInt("id_contract"));
        contract.setCode_contract(rs.getString("code_contract"));

        // 1. Ép kiểu Timestamp từ DB sang LocalDateTime của Java 8+
        if (rs.getTimestamp("start_datetime") != null)
        {
            contract.setStart_datetime(rs.getTimestamp("start_datetime").toLocalDateTime());
        }

        if (rs.getTimestamp("end_datetime") != null)
        {
            contract.setEnd_datetime(rs.getTimestamp("end_datetime").toLocalDateTime());
        }

        if (rs.getTimestamp("return_datetime") != null)
        {
            contract.setReturn_datetime(rs.getTimestamp("return_datetime").toLocalDateTime());
        }

        // 2. Map các dữ liệu số nguyên và số thực
        contract.setKm_start(rs.getInt("km_start"));
        contract.setKm_end(rs.getInt("km_end"));
        contract.setFuel_start(rs.getInt("fuel_start"));
        contract.setFuel_end(rs.getInt("fuel_end"));
        contract.setDeposit_amount(rs.getDouble("deposit_amount"));
        contract.setBase_price(rs.getDouble("base_price"));
        contract.setDiscount_amount(rs.getDouble("discount_amount"));
        contract.setTotal_price(rs.getDouble("total_price"));

        // 3. Map các dữ liệu Enum (Check null trước khi ép kiểu để tránh Crash)
        if (rs.getString("deposit_type") != null)
        {
            contract.setDeposit_type(DepositType.valueOf(rs.getString("deposit_type")));
        }

        if (rs.getString("payment_status") != null)
        {
            contract.setPayment_status(PaymentStatus.valueOf(rs.getString("payment_status")));
        }

        if (rs.getString("status") != null)
        {
            contract.setStatus(StatusContracts.valueOf(rs.getString("status")));
        }

        // 4. Map Khóa ngoại (Khởi tạo Object trống và nhét ID vào để xài tạm)
        Users user = new Users();
        user.setId_user(rs.getInt("id_user"));
        contract.setId_user(user);

        Vehicles vehicle = new Vehicles();
        vehicle.setId_vehicle(rs.getInt("id_vehicle"));
        contract.setId_vehicle(vehicle);

        Customers customer = new Customers();
        customer.setId_customer(rs.getInt("id_customer"));
        contract.setId_customer(customer);

        // Voucher có thể Null (do Khách không có mã giảm giá)
        int idVoucher = rs.getInt("id_voucher");
        if (!rs.wasNull())
        {
            Vouchers voucher = new Vouchers();
            voucher.setId_voucher(idVoucher);
            contract.setId_voucher(voucher);
        }

        return contract;
    }

    // ==========================================================
    // CÁC HÀM IMPLEMENTS TỪ IBaseDAO
    // ==========================================================

    @Override
    public boolean insert(Contracts entity)
    {
        // Khi tạo mới Hợp đồng: return_datetime, km_end, fuel_end thường bằng 0 hoặc null
        String sql = "INSERT INTO Contracts (code_contract, start_datetime, end_datetime, km_start, fuel_start, deposit_type, deposit_amount, base_price, discount_amount, total_price, payment_status, status, id_user, id_vehicle, id_customer, id_voucher) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setString(1, entity.getCode_contract());

            // Chuyển LocalDateTime thành Timestamp để lưu xuống MySQL
            pstm.setTimestamp(2, Timestamp.valueOf(entity.getStart_datetime()));
            pstm.setTimestamp(3, Timestamp.valueOf(entity.getEnd_datetime()));

            pstm.setInt(4, entity.getKm_start());
            pstm.setInt(5, entity.getFuel_start());

            // Lấy chuỗi String từ Enum
            pstm.setString(6, entity.getDeposit_type().name());

            pstm.setDouble(7, entity.getDeposit_amount());
            pstm.setDouble(8, entity.getBase_price());
            pstm.setDouble(9, entity.getDiscount_amount());
            pstm.setDouble(10, entity.getTotal_price());
            pstm.setString(11, entity.getPayment_status().name());
            pstm.setString(12, entity.getStatus().name());

            // Rút trích ID từ các Object tham chiếu (Khóa ngoại)
            pstm.setInt(13, entity.getId_user().getId_user());
            pstm.setInt(14, entity.getId_vehicle().getId_vehicle());
            pstm.setInt(15, entity.getId_customer().getId_customer());

            // Xử lý Voucher Null an toàn
            if (entity.getId_voucher() != null && entity.getId_voucher().getId_voucher() > 0)
            {
                pstm.setInt(16, entity.getId_voucher().getId_voucher());
            }
            else
            {
                pstm.setNull(16, java.sql.Types.INTEGER);
            }

            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Tạo Hợp đồng mới: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Contracts entity)
    {
        // Thường gọi lúc GIA HẠN THUÊ hoặc TRẢ XE để cập nhật kết quả cuối cùng
        String sql = "UPDATE Contracts SET end_datetime = ?, return_datetime = ?, km_end = ?, fuel_end = ?, discount_amount = ?, total_price = ?, payment_status = ?, status = ? WHERE id_contract = ?";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setTimestamp(1, Timestamp.valueOf(entity.getEnd_datetime()));

            if (entity.getReturn_datetime() != null)
            {
                pstm.setTimestamp(2, Timestamp.valueOf(entity.getReturn_datetime()));
            }
            else
            {
                pstm.setNull(2, java.sql.Types.TIMESTAMP);
            }

            pstm.setInt(3, entity.getKm_end());
            pstm.setInt(4, entity.getFuel_end());
            pstm.setDouble(5, entity.getDiscount_amount());
            pstm.setDouble(6, entity.getTotal_price());
            pstm.setString(7, entity.getPayment_status().name());
            pstm.setString(8, entity.getStatus().name());

            // Khóa chính nằm cuối cùng trong chuỗi WHERE
            pstm.setInt(9, entity.getId_contract());

            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Cập nhật Hợp đồng: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Integer id)
    {
        // Nghiệp vụ Hủy Hợp đồng: Đổi Enum Status sang CANCELLED (Hoặc giá trị tương đương trong Enum của bạn em)
        String sql = "UPDATE Contracts SET status = 'CANCELLED' WHERE id_contract = ?";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Hủy Hợp đồng: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Contracts findById(Integer id)
    {
        String sql = "SELECT * FROM Contracts WHERE id_contract = ?";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);

            try (ResultSet rs = pstm.executeQuery())
            {
                if (rs.next())
                {
                    return mapResultSetToContract(rs);
                }
            }
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Tìm Hợp đồng theo ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Contracts> findAll()
    {
        List<Contracts> listContracts = new ArrayList<>();
        // Sắp xếp ID giảm dần (DESC) để Hợp đồng mới tạo luôn nổi lên trên cùng màn hình
        String sql = "SELECT * FROM Contracts ORDER BY id_contract DESC";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery())
        {
            while (rs.next())
            {
                listContracts.add(mapResultSetToContract(rs));
            }
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Lấy danh sách Hợp đồng: " + e.getMessage());
        }
        return listContracts;
    }

    // ==========================================================
    // PHẦN 3: CÁC HÀM THỐNG KÊ (Dùng cho Dashboard)
    // ==========================================================

    public double getTodayRevenue()
    {
        // Tính tổng tiền của các hợp đồng được tạo trong ngày hôm nay (loại trừ các hợp đồng đã Hủy)
        // Lưu ý: CURDATE() là hàm lấy ngày hiện tại của MySQL
        String sql = "SELECT SUM(total_price) FROM Contracts WHERE DATE(start_datetime) = CURDATE() AND status != 'CANCELLED'";

        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery())
        {
            if (rs.next())
            {
                return rs.getDouble(1); // Lấy giá trị của cột SUM
            }
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Tính doanh thu hôm nay: " + e.getMessage());
        }
        return 0.0; // Trả về 0 nếu hôm nay chưa có đơn nào
    }
}