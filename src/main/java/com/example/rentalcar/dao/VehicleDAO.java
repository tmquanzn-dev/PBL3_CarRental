//package com.example.rentalcar.dao;
//
//import com.example.rentalcar.models.StatusVehicle;
//import com.example.rentalcar.models.Vehicles;
//import com.example.rentalcar.utils.DBConnection;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class VehicleDAO {
//    public List<Vehicles> getAllVehicle() {
//        List<Vehicles> list = new ArrayList();
//        String sql = "Select * from Vehicles";
//        Connection cnt = DBConnection.getConnection();
//        if (cnt == null) {
//            System.out.println("LỖI! Không thể kết nối tới DATABASE");
//            return null;
//        }
//        try (PreparedStatement pstm = cnt.prepareStatement(sql)) {
//            ResultSet rs = pstm.executeQuery();
//            while (rs.next()) {
//                Vehicles vehicle = new Vehicles();
//                vehicle.setId_vehicle(rs.getInt("id_vehicle"));
//                vehicle.setCode_vehicle(rs.getString("code_vehicle"));
//                vehicle.setBrand(rs.getString("brand"));
//                vehicle.setModel(rs.getString("model"));
//                vehicle.setColor(rs.getString("color"));
//                vehicle.setYear_of_manufacture(rs.getInt("year_of_manufacture"));
//                vehicle.setPrice_day(rs.getDouble("price_day"));
//                vehicle.setPrice_hour(rs.getDouble("price_hour"));
//                vehicle.setCurrent_km(rs.getInt("current_km"));
//                vehicle.setFuel_capacity(rs.getInt("fuel_capacity"));
//                vehicle.setTotal_price(rs.getDouble("total_price"));
//                String StatusStr = rs.getString("status");
//                if (StatusStr != null) {
//                    vehicle.setStatus(StatusVehicle.valueOf(StatusStr));
//                }
//                list.add(vehicle);
//            }
//            return list;
//        } catch (SQLException e) {
//            System.out.println("Lỗi truy vấn SQL: " + e.getMessage());
//        }
//        return null;
//    }
//    public boolean checkCodeVehicle(Vehicles v) {
//        String sql = "Select
//        return false;
//    }
//    public void insertVehicle(Vehicles v) {
//
//        String sql = "insert into Vehicles (id_vehicle, code_vehicle, brand, model, color, year_of_manufacture," +
//                " price_day, price_hour, fuel_capacity, current_km, status, total_price)" +
//                " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        Connection cnt = DBConnection.getConnection();
//        if (cnt == null) {
//            System.err.println("LỖI! Không thể kết nối đến DATABASE");
//        }
//        try (PreparedStatement pstm = cnt.prepareStatement(sql)) {
//
//
//        } catch (SQLException e) {
//            System.out.println("Lỗi truy vấn SQL: " + e.getMessage());
//        }
//
//    }
//}
