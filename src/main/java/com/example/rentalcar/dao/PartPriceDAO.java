package com.example.rentalcar.dao;

import com.example.rentalcar.models.PartPrices;
import com.example.rentalcar.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartPriceDAO implements IBaseDAO<PartPrices, Integer>
{
    private PartPrices mapResultSetToPartPrice(ResultSet rs) throws SQLException {
        PartPrices pp = new PartPrices();
        pp.setId_part_price(rs.getInt("id_part_price"));
        pp.setPart_name(rs.getString("part_name"));
        // Map cột vehicle_type từ DB vào biến vehicle của Model
        pp.setVehicle(rs.getString("vehicle_type"));
        pp.setPrice(rs.getDouble("price"));
        return pp;
    }

    @Override
    public boolean insert(PartPrices entity) {
        String sql = "INSERT INTO partprices (part_name, vehicle_type, price) VALUES (?, ?, ?)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setString(1, entity.getPart_name());
            pstm.setString(2, entity.getVehicle());
            pstm.setDouble(3, entity.getPrice());
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean update(PartPrices entity) {
        String sql = "UPDATE partprices SET part_name = ?, vehicle_type = ?, price = ? WHERE id_part_price = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setString(1, entity.getPart_name());
            pstm.setString(2, entity.getVehicle());
            pstm.setDouble(3, entity.getPrice());
            pstm.setInt(4, entity.getId_part_price());
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM partprices WHERE id_part_price = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public PartPrices findById(Integer id) {
        String sql = "SELECT * FROM partprices WHERE id_part_price = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapResultSetToPartPrice(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<PartPrices> findAll() {
        List<PartPrices> list = new ArrayList<>();
        String sql = "SELECT * FROM partprices";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToPartPrice(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}