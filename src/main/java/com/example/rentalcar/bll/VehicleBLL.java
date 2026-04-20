package com.example.rentalcar.bll;

import com.example.rentalcar.dao.VehicleDAO;
import com.example.rentalcar.models.Vehicles;
import java.util.List;
import java.util.stream.Collectors;

public class VehicleBLL {
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    // ==========================================
    // 1. CÁC HÀM TRUY VẤN CƠ BẢN
    // ==========================================

    public List<Vehicles> getAllVehicles() {
        return vehicleDAO.findAll();
    }

    public Vehicles getVehicleById(int id) {
        return vehicleDAO.findById(id);
    }

    // ==========================================
    // 2. LOGIC NGHIỆP VỤ (BUSINESS LOGIC)
    // ==========================================

    /**
     * Kiểm tra và thêm xe mới với đầy đủ ràng buộc theo BA
     */
    public boolean addVehicle(Vehicles vehicle) {
        validateVehicle(vehicle);
        return vehicleDAO.insert(vehicle);
    }

    public boolean updateVehicle(Vehicles vehicle) {
        validateVehicle(vehicle);
        return vehicleDAO.update(vehicle);
    }

    /**
     * Logic kiểm tra dữ liệu đầu vào (Validation)
     */
    private void validateVehicle(Vehicles vehicle) {
        if (vehicle.getCode_vehicle() == null || vehicle.getCode_vehicle().isBlank())
            throw new IllegalArgumentException("Biển số xe không được để trống!");

        if (vehicle.getVehicle_type() == null || vehicle.getVehicle_type().isBlank())
            throw new IllegalArgumentException("Loại xe (Tay ga/Xe số) không được để trống!");

        if (vehicle.getPrice_day() <= 0 || vehicle.getPrice_hour() <= 0)
            throw new IllegalArgumentException("Đơn giá thuê phải lớn hơn 0!");

        if (vehicle.getPurchase_price() <= 0)
            throw new IllegalArgumentException("Giá trị mua xe (để tính ROI) phải lớn hơn 0!");

        if (vehicle.getMaintenance_km() <= vehicle.getCurrent_km())
            System.out.println("Cảnh báo: Xe này cần bảo dưỡng ngay lập tức!");
    }

    /**
     * Lấy danh sách xe cần bảo dưỡng (Current KM >= Maintenance KM)
     */
    public List<Vehicles> getVehiclesNeedMaintenance() {
        return vehicleDAO.findAll().stream()
                .filter(v -> v.getCurrent_km() >= v.getMaintenance_km())
                .collect(Collectors.toList());
    }

    /**
     * Hàm lọc xe linh hoạt cho giao diện (Search & Filter)
     */
    public List<Vehicles> searchVehicles(String keyword, String brand, String status) {
        return vehicleDAO.findAll().stream()
                .filter(v -> (keyword == null || keyword.isEmpty() ||
                        v.getCode_vehicle().toLowerCase().contains(keyword.toLowerCase()) ||
                        v.getModel().toLowerCase().contains(keyword.toLowerCase())))
                .filter(v -> (brand == null || brand.equals("Tất cả") || v.getBrand().equalsIgnoreCase(brand)))
                .filter(v -> (status == null || status.equals("Tất cả") || v.getStatus().name().equalsIgnoreCase(status)))
                .collect(Collectors.toList());
    }

    // ==========================================
    // 3. THỐNG KÊ (DASHBOARD)
    // ==========================================

    public int getTotalActiveCars() { return vehicleDAO.getTotalActiveCars(); }
    public int getRentedCars() { return vehicleDAO.getRentedCars(); }
    public int getAvailableCars() { return vehicleDAO.getAvailableCars(); }

    public boolean deleteVehicle(int id) {
        return vehicleDAO.delete(id);
    }
}