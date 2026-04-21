package com.example.rentalcar.dao;

import com.example.rentalcar.models.*;
import com.example.rentalcar.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InspectionDAO implements IBaseDAO<Inspections, Integer>
{
    private Inspections mapResultSetToInspection(ResultSet rs) throws SQLException {
        Inspections ins = new Inspections();
        ins.setId_inspection(rs.getInt("id_inspection"));

        // Vì Model đặt tên biến là id_contract nhưng kiểu là Contracts
        Contracts contract = new Contracts();
        contract.setId_contract(rs.getInt("id_contract"));
        ins.setId_contract(contract);

        Users user = new Users();
        user.setId_user(rs.getInt("id_user"));
        ins.setId_user(user);

        String typeStr = rs.getString("inspection_type");
        if (typeStr != null) {
            ins.setInspection_type(InspectionType.valueOf(typeStr.replace(" ", "_")));
        }
        return ins;
    }

    @Override
    public boolean insert(Inspections entity) {
        String sql = "INSERT INTO inspections (id_contract, id_user, inspection_type) VALUES (?, ?, ?)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, entity.getId_contract().getId_contract());
            pstm.setInt(2, entity.getId_user().getId_user());
            pstm.setString(3, entity.getInspection_type().name().replace("_", " "));
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean update(Inspections entity) {
        String sql = "UPDATE inspections SET id_contract = ?, id_user = ?, inspection_type = ? WHERE id_inspection = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, entity.getId_contract().getId_contract());
            pstm.setInt(2, entity.getId_user().getId_user());
            pstm.setString(3, entity.getInspection_type().name().replace("_", " "));
            pstm.setInt(4, entity.getId_inspection());
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM inspections WHERE id_inspection = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public Inspections findById(Integer id) {
        String sql = "SELECT * FROM inspections WHERE id_inspection = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapResultSetToInspection(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Inspections> findAll() {
        List<Inspections> list = new ArrayList<>();
        String sql = "SELECT * FROM inspections";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToInspection(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}