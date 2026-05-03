package com.example.rentalcar.bll;

import com.example.rentalcar.dao.VehicleDAO;
import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.models.Vehicles;

import java.util.List;
import java.util.stream.Collectors;

public class VehicleBLL {
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    // ==========================================
    // 1. CÁC HÀM TRUY VẤN
    // ==========================================

    /**
     * Lấy xe KHÔNG bao gồm INACTIVE.
     * Dùng cho: tạo hợp đồng, dashboard, báo cáo.
     */
    public List<Vehicles> getAllVehicles() {
        return vehicleDAO.findAll();
    }

    /**
     * Lấy xe BAO GỒM cả INACTIVE.
     * Dùng cho: màn hình Quản lý xe.
     */
    public List<Vehicles> getAllVehiclesIncludeInactive() {
        return vehicleDAO.findAllIncludeInactive();
    }

    public Vehicles getVehicleById(int id) {
        return vehicleDAO.findById(id);
    }

    // ==========================================
    // 2. LOGIC NGHIỆP VỤ
    // ==========================================

    public boolean addVehicle(Vehicles vehicle) {
        validateVehicle(vehicle);
        return vehicleDAO.insert(vehicle);
    }

    public boolean updateVehicle(Vehicles vehicle) {
        validateVehicle(vehicle);
        return vehicleDAO.update(vehicle);
    }

    private void validateVehicle(Vehicles vehicle) {
        if (vehicle.getCode_vehicle() == null || vehicle.getCode_vehicle().isBlank())
            throw new IllegalArgumentException("Biển số xe không được để trống!");
        if (vehicle.getVehicle_type() == null || vehicle.getVehicle_type().isBlank())
            throw new IllegalArgumentException("Loại xe không được để trống!");
        if (vehicle.getPrice_day() <= 0 || vehicle.getPrice_hour() <= 0)
            throw new IllegalArgumentException("Đơn giá thuê phải lớn hơn 0!");
        if (vehicle.getPurchase_price() <= 0)
            throw new IllegalArgumentException("Giá trị mua xe phải lớn hơn 0!");
        if (vehicle.getMaintenance_km() <= vehicle.getCurrent_km())
            System.out.println("Cảnh báo: Xe này cần bảo dưỡng ngay!");
    }

    public List<Vehicles> getVehiclesNeedMaintenance() {
        return vehicleDAO.findAll().stream()
                .filter(v -> v.getCurrent_km() >= v.getMaintenance_km())
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm trên màn hình Quản lý xe.
     * Dùng findAllIncludeInactive để lọc được cả xe INACTIVE.
     */
    public List<Vehicles> searchVehicles(String keyword, String brand, String statusDisplay) {
        return vehicleDAO.findAllIncludeInactive().stream()
                .filter(v -> {
                    if (keyword == null || keyword.isBlank()) return true;
                    String kw = keyword.toLowerCase();
                    boolean matchCode  = v.getCode_vehicle() != null && v.getCode_vehicle().toLowerCase().contains(kw);
                    boolean matchModel = v.getModel() != null && v.getModel().toLowerCase().contains(kw);
                    boolean matchBrand = v.getBrand() != null && v.getBrand().toLowerCase().contains(kw);
                    return matchCode || matchModel || matchBrand;
                })
                .filter(v -> {
                    if (brand == null || brand.isBlank() || brand.equals("Tất cả")) return true;
                    return v.getBrand() != null && v.getBrand().equalsIgnoreCase(brand);
                })
                .filter(v -> {
                    if (statusDisplay == null || statusDisplay.isBlank() || statusDisplay.equals("Tất cả")) return true;
                    StatusVehicle target = displayToStatus(statusDisplay);
                    return target != null && v.getStatus() == target;
                })
                .collect(Collectors.toList());
    }

    public static StatusVehicle displayToStatus(String display) {
        if (display == null) return null;
        return switch (display.trim()) {
            case "Sẵn sàng",        "AVAILABLE"   -> StatusVehicle.AVAILABLE;
            case "Đang thuê",       "RENTED"      -> StatusVehicle.RENTED;
            case "Bảo dưỡng",       "MAINTENANCE" -> StatusVehicle.MAINTENANCE;
            case "Đặt trước",       "RESERVED"    -> StatusVehicle.RESERVED;
            case "Ngừng hoạt động", "INACTIVE"    -> StatusVehicle.INACTIVE;
            default                               -> null;
        };
    }

    public static String statusToDisplay(StatusVehicle s) {
        if (s == null) return "--";
        return switch (s) {
            case AVAILABLE   -> "Sẵn sàng";
            case RENTED      -> "Đang thuê";
            case MAINTENANCE -> "Bảo dưỡng";
            case RESERVED    -> "Đặt trước";
            case INACTIVE    -> "Ngừng hoạt động";
        };
    }

    // ==========================================
    // 3. THỐNG KÊ DASHBOARD
    // ==========================================

    public int getTotalActiveCars() { return vehicleDAO.getTotalActiveCars(); }
    public int getRentedCars()      { return vehicleDAO.getRentedCars(); }
    public int getAvailableCars()   { return vehicleDAO.getAvailableCars(); }

    public boolean deleteVehicle(int id) {
        return vehicleDAO.delete(id);
    }
}