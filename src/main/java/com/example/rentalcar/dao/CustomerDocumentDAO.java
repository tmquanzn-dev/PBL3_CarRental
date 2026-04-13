package com.example.rentalcar.dao;

import com.example.rentalcar.models.CustomerDocuments;
import com.example.rentalcar.models.Customers;
import com.example.rentalcar.models.DocumentType;
import com.example.rentalcar.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDocumentDAO implements IBaseDAO<CustomerDocuments, Integer>
{
    private CustomerDocuments mapResultSetToDocument(ResultSet rs) throws SQLException {
        CustomerDocuments doc = new CustomerDocuments();
        doc.setId_document(rs.getInt("id_document"));

        // Tạo đối tượng Customers "vỏ" để chứa ID
        Customers customer = new Customers();
        customer.setId_customer(rs.getInt("id_customer"));
        doc.setCustomers(customer);

        // Map Enum: DB "BANG LAI" -> Java BANG_LAI
        String typeStr = rs.getString("document_type");
        if (typeStr != null) {
            doc.setDocument_type(DocumentType.valueOf(typeStr.replace(" ", "_")));
        }

        doc.setDocument_number(rs.getString("document_number"));
        return doc;
    }

    @Override
    public boolean insert(CustomerDocuments entity) {
        String sql = "INSERT INTO customerdocuments (id_customer, document_type, document_number) VALUES (?, ?, ?)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, entity.getCustomers().getId_customer());
            pstm.setString(2, entity.getDocument_type().name().replace("_", " "));
            pstm.setString(3, entity.getDocument_number());
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean update(CustomerDocuments entity) {
        String sql = "UPDATE customerdocuments SET id_customer = ?, document_type = ?, document_number = ? WHERE id_document = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, entity.getCustomers().getId_customer());
            pstm.setString(2, entity.getDocument_type().name().replace("_", " "));
            pstm.setString(3, entity.getDocument_number());
            pstm.setInt(4, entity.getId_document());
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM customerdocuments WHERE id_document = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public CustomerDocuments findById(Integer id) {
        String sql = "SELECT * FROM customerdocuments WHERE id_document = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapResultSetToDocument(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<CustomerDocuments> findAll() {
        List<CustomerDocuments> list = new ArrayList<>();
        String sql = "SELECT * FROM customerdocuments";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToDocument(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}