package com.example.rentalcar.dao;

import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.models.Vehicles;
import com.example.rentalcar.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO implements IBaseDAO<Vehicles, Integer> {

    // ==========================================================
    // PHẦN 1: CÁC HÀM THỐNG KÊ DASHBOARD
    // ==========================================================

    private int countVehiclesByCondition(String condition) {
        String sql = "SELECT COUNT(*) FROM vehicles WHERE " + condition;
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("LỖI truy vấn thống kê xe: " + e.getMessage());
        }
        return 0;
    }

    public int getTotalActiveCars() { return countVehiclesByCondition("status != 'INACTIVE'"); }
    public int getRentedCars() { return countVehiclesByCondition("status = 'RENTED'"); }
    public int getAvailableCars() { return countVehiclesByCondition("status = 'AVAILABLE'"); }

    // ==========================================================
    // PHẦN 2: CÁC HÀM IMPLEMENTS TỪ IBaseDAO
    // ==========================================================

    private Vehicles mapResultSetToVehicle(ResultSet rs) throws SQLException {
        Vehicles car = new Vehicles();
        car.setId_vehicle(rs.getInt("id_vehicle"));
        car.setCode_vehicle(rs.getString("code_vehicle"));
        car.setBrand(rs.getString("brand"));
        car.setModel(rs.getString("model"));
        car.setVehicle_type(rs.getString("vehicle_type"));
        car.setColor(rs.getString("color"));
        car.setYear_of_manufacture(rs.getInt("year_of_manufacture"));
        car.setPrice_day(rs.getDouble("price_day"));
        car.setPrice_hour(rs.getDouble("price_hour"));
        car.setFuel_capacity(rs.getInt("fuel_capacity"));
        car.setCurrent_km(rs.getInt("current_km"));
        car.setMaintenance_km(rs.getInt("maintenance_km"));
        car.setImage_url(rs.getString("image_url"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            car.setStatus(StatusVehicle.valueOf(statusStr));
        }

        car.setPurchase_price(rs.getDouble("purchase_price"));
        return car;
    }

    @Override
    public boolean insert(Vehicles entity) {
        String sql = "INSERT INTO vehicles (code_vehicle, brand, model, vehicle_type, color, year_of_manufacture, " +
                "price_day, price_hour, fuel_capacity, current_km, maintenance_km, image_url, status, purchase_price) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setString(1, entity.getCode_vehicle());
            pstm.setString(2, entity.getBrand());
            pstm.setString(3, entity.getModel());
            pstm.setString(4, entity.getVehicle_type());
            pstm.setString(5, entity.getColor());
            pstm.setInt(6, entity.getYear_of_manufacture());
            pstm.setDouble(7, entity.getPrice_day());
            pstm.setDouble(8, entity.getPrice_hour());
            pstm.setInt(9, entity.getFuel_capacity());
            pstm.setInt(10, entity.getCurrent_km());
            pstm.setInt(11, entity.getMaintenance_km());
            pstm.setString(12, entity.getImage_url());
            pstm.setString(13, entity.getStatus().name());
            pstm.setDouble(14, entity.getPurchase_price()); // Lưu purchase_price

            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI Thêm xe mới: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Vehicles entity) {
        String sql = "UPDATE vehicles SET brand = ?, model = ?, vehicle_type = ?, color = ?, year_of_manufacture = ?, " +
                "price_day = ?, price_hour = ?, fuel_capacity = ?, current_km = ?, maintenance_km = ?, " +
                "image_url = ?, status = ?, purchase_price = ? WHERE id_vehicle = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setString(1, entity.getBrand());
            pstm.setString(2, entity.getModel());
            pstm.setString(3, entity.getVehicle_type());
            pstm.setString(4, entity.getColor());
            pstm.setInt(5, entity.getYear_of_manufacture());
            pstm.setDouble(6, entity.getPrice_day());
            pstm.setDouble(7, entity.getPrice_hour());
            pstm.setInt(8, entity.getFuel_capacity());
            pstm.setInt(9, entity.getCurrent_km());
            pstm.setInt(10, entity.getMaintenance_km());
            pstm.setString(11, entity.getImage_url());
            pstm.setString(12, entity.getStatus().name());
            pstm.setDouble(13, entity.getPurchase_price());
            pstm.setInt(14, entity.getId_vehicle());

            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI Cập nhật xe: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "UPDATE vehicles SET status = 'INACTIVE' WHERE id_vehicle = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, id);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("LỖI Xóa xe: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Vehicles findById(Integer id) {
        String sql = "SELECT * FROM vehicles WHERE id_vehicle = ?";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) return mapResultSetToVehicle(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Vehicles> findAll() {
        List<Vehicles> listCars = new ArrayList<>();
        String sql = "SELECT * FROM vehicles WHERE status != 'INACTIVE'";
        try (Connection cnt = DBConnection.getInstance().getConnection();
             PreparedStatement pstm = cnt.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) {
                listCars.add(mapResultSetToVehicle(rs));
            }
        } catch (SQLException e) {
            System.err.println("LỖI Lấy danh sách xe: " + e.getMessage());
        }
        return listCars;
    }
}