package com.example.rentalcar.dao;

import com.example.rentalcar.models.Contracts;
import com.example.rentalcar.models.PenaltyType;
import com.example.rentalcar.models.Penalties;
import com.example.rentalcar.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PenaltyDAO implements IBaseDAO<Penalties, Integer>
{
    // ==========================================================
    // HÀM MAP DỮ LIỆU (Hô biến Row DB thành Object Java)
    // ==========================================================
    private Penalties mapResultSetToPenalty(ResultSet rs) throws SQLException
    {
        Penalties p = new Penalties();
        p.setId_penalty(rs.getInt("id_penalty"));

        // Khởi tạo đối tượng Contract "vỏ" để lưu ID
        // Sau này tầng BLL sẽ dùng ID này để load chi tiết hợp đồng nếu cần
        Contracts contract = new Contracts();
        contract.setId_contract(rs.getInt("id_contract"));
        p.setId_contract(contract);

        // Chuyển đổi Enum: Từ "QUA GIO" (DB) sang QUA_GIO (Java)
        String dbType = rs.getString("penalty_type");
        if (dbType != null)
        {
            p.setPenalty_type(PenaltyType.valueOf(dbType.replace(" ", "_")));
        }

        p.setAmount(rs.getDouble("amount"));
        return p;
    }

    // ==========================================================
    // HÀM INSERT (Thêm biên bản phạt mới)
    // ==========================================================
    @Override
    public boolean insert(Penalties entity)
    {
        String sql = "INSERT INTO penalties (id_contract, penalty_type, amount) VALUES (?, ?, ?)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, entity.getId_contract().getId_contract());
            pstm.setString(2, entity.getPenalty_type().name().replace("_", " "));
            pstm.setDouble(3, entity.getAmount());
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Thêm Penalty: " + e.getMessage());
            return false;
        }
    }

    // ==========================================================
    // HÀM UPDATE (Cập nhật thông tin phạt)
    // ==========================================================
    @Override
    public boolean update(Penalties entity)
    {
        String sql = "UPDATE penalties SET id_contract = ?, penalty_type = ?, amount = ? WHERE id_penalty = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, entity.getId_contract().getId_contract());
            pstm.setString(2, entity.getPenalty_type().name().replace("_", " "));
            pstm.setDouble(3, entity.getAmount());
            pstm.setInt(4, entity.getId_penalty());
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Cập nhật Penalty: " + e.getMessage());
            return false;
        }
    }

    // ==========================================================
    // HÀM DELETE (Xóa biên bản phạt)
    // ==========================================================
    @Override
    public boolean delete(Integer id)
    {
        String sql = "DELETE FROM penalties WHERE id_penalty = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Xóa Penalty: " + e.getMessage());
            return false;
        }
    }

    // ==========================================================
    // HÀM FIND BY ID (Tìm 1 biên bản phạt cụ thể)
    // ==========================================================
    @Override
    public Penalties findById(Integer id)
    {
        String sql = "SELECT * FROM penalties WHERE id_penalty = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery())
            {
                if (rs.next()) return mapResultSetToPenalty(rs);
            }
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Tìm Penalty theo ID: " + e.getMessage());
        }
        return null;
    }

    // ==========================================================
    // HÀM FIND ALL (Lấy toàn bộ danh sách phạt)
    // ==========================================================
    @Override
    public List<Penalties> findAll()
    {
        List<Penalties> list = new ArrayList<>();
        String sql = "SELECT * FROM penalties";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery())
        {
            while (rs.next())
            {
                list.add(mapResultSetToPenalty(rs));
            }
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Lấy danh sách Penalty: " + e.getMessage());
        }
        return list;
    }

    // ==========================================================
    // HÀM ĐẶC THÙ (Tìm tất cả lỗi phạt của 1 hợp đồng)
    // ==========================================================
    public List<Penalties> findByContractId(int id_contract)
    {
        List<Penalties> list = new ArrayList<>();
        String sql = "SELECT * FROM penalties WHERE id_contract = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql))
        {
            pstm.setInt(1, id_contract);
            try (ResultSet rs = pstm.executeQuery())
            {
                while (rs.next())
                {
                    list.add(mapResultSetToPenalty(rs));
                }
            }
        }
        catch (SQLException e)
        {
            System.err.println("LỖI Tìm Penalty theo Contract ID: " + e.getMessage());
        }
        return list;
    }
}