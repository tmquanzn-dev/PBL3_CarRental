package com.example.rentalcar.bll;

import com.example.rentalcar.dao.VehicleDAO;
import com.example.rentalcar.models.Vehicles;
import java.util.List;

public class VehicleBLL {
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    public int getTotalActiveCars() {
        return vehicleDAO.getTotalActiveCars();
    }

    public int getRentedCars() {
        return vehicleDAO.getRentedCars();
    }

    public int getAvailableCars() {
        return vehicleDAO.getAvailableCars();
    }

    public List<Vehicles> getAllVehicles() {
        return vehicleDAO.findAll();
    }

    public Vehicles getVehicleById(int id) {
        return vehicleDAO.findById(id);
    }

    public boolean addVehicle(Vehicles vehicle) {
        if (vehicle.getCode_vehicle() == null || vehicle.getCode_vehicle().isBlank())
            throw new IllegalArgumentException("Biển số xe không được để trống!");
        if (vehicle.getPrice_day() <= 0)
            throw new IllegalArgumentException("Giá thuê theo ngày phải lớn hơn 0!");
        return vehicleDAO.insert(vehicle);
    }

    public boolean updateVehicle(Vehicles vehicle) {
        return vehicleDAO.update(vehicle);
    }

    public boolean deleteVehicle(int id) {
        return vehicleDAO.delete(id);
    }
}