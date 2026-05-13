package com.example.rentalcar.bll;

import com.example.rentalcar.dao.VehicleDAO;
import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.models.Vehicles;
import com.example.rentalcar.utils.AppSession;

import java.util.List;
import java.util.stream.Collectors;

public class VehicleBLL {
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    // ==========================================
    // 1. TRUY VẤN
    // ==========================================
    public List<Vehicles> getAllVehicles() {
        return vehicleDAO.findAll();
    }

    public List<Vehicles> getAllVehiclesIncludeInactive() {
        return vehicleDAO.findAllIncludeInactive();
    }

    public Vehicles getVehicleById(int id) {
        return vehicleDAO.findById(id);
    }

    // ==========================================
    // 2. THÊM / SỬA XE
    // ==========================================
    public boolean addVehicle(Vehicles vehicle) {
        if (!AppSession.isAdmin())
            throw new IllegalStateException("Chỉ Admin mới được thêm xe mới!");
        validateVehicle(vehicle);
        return vehicleDAO.insert(vehicle);
    }

    public boolean updateVehicle(Vehicles vehicle) {
        if (!AppSession.isAdmin())
            throw new IllegalStateException("Chỉ Admin mới được sửa thông tin xe!");
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
    }

    // ==========================================
    // 3. BẢO DƯỠNG
    // ==========================================

    /**
     * Quét toàn bộ xe AVAILABLE có current_km >= maintenance_km
     * → tự động chuyển sang MAINTENANCE.
     * Trả về danh sách xe vừa được đánh dấu.
     */
    public List<Vehicles> checkAndMarkMaintenance() {
        List<Vehicles> needMaintenance = vehicleDAO.findAll().stream()
                .filter(v -> v.getStatus() == StatusVehicle.AVAILABLE
                        && v.getCurrent_km() >= v.getMaintenance_km())
                .collect(Collectors.toList());

        for (Vehicles v : needMaintenance) {
            v.setStatus(StatusVehicle.MAINTENANCE);
            vehicleDAO.update(v);
        }

        return needMaintenance;
    }

    /**
     * Hoàn thành bảo dưỡng:
     * - Chuyển xe về AVAILABLE
     * - Mốc bảo dưỡng tiếp theo = current_km + 5000
     *   VD: xe đang ở 5100 km → mốc tiếp theo = 10100 km
     */
    public boolean completeMaintenance(int vehicleId) {
        Vehicles vehicle = vehicleDAO.findById(vehicleId);
        if (vehicle == null)
            throw new IllegalArgumentException("Không tìm thấy xe!");
        if (vehicle.getStatus() != StatusVehicle.MAINTENANCE)
            throw new IllegalStateException("Xe này không đang trong trạng thái bảo dưỡng!");

        // Mốc tiếp theo = km hiện tại + 5000
        int nextMaintenanceKm = vehicle.getCurrent_km() + 5000;
        vehicle.setMaintenance_km(nextMaintenanceKm);
        vehicle.setStatus(StatusVehicle.AVAILABLE);

        return vehicleDAO.update(vehicle);
    }

    /**
     * Lấy danh sách xe đang MAINTENANCE.
     */
    public List<Vehicles> getMaintenanceVehicles() {
        return vehicleDAO.findAllIncludeInactive().stream()
                .filter(v -> v.getStatus() == StatusVehicle.MAINTENANCE)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách xe AVAILABLE nhưng đã đạt/vượt mốc km bảo dưỡng.
     */
    public List<Vehicles> getVehiclesNeedMaintenance() {
        return vehicleDAO.findAll().stream()
                .filter(v -> v.getStatus() == StatusVehicle.AVAILABLE
                        && v.getCurrent_km() >= v.getMaintenance_km())
                .collect(Collectors.toList());
    }

    // ==========================================
    // 4. TÌM KIẾM
    // ==========================================
    public List<Vehicles> searchVehicles(String keyword, String brand, String statusDisplay) {
        return vehicleDAO.findAllIncludeInactive().stream()
                .filter(v -> {
                    if (keyword == null || keyword.isBlank()) return true;
                    String kw = keyword.toLowerCase();
                    return (v.getCode_vehicle() != null && v.getCode_vehicle().toLowerCase().contains(kw))
                            || (v.getModel() != null && v.getModel().toLowerCase().contains(kw))
                            || (v.getBrand() != null && v.getBrand().toLowerCase().contains(kw));
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

    // ==========================================
    // 5. HELPER CONVERT
    // ==========================================
    public static StatusVehicle displayToStatus(String display) {
        if (display == null) return null;
        return switch (display.trim()) {
            case "Sẵn sàng",        "AVAILABLE"   -> StatusVehicle.AVAILABLE;
            case "Đang thuê",       "RENTED"      -> StatusVehicle.RENTED;
            case "Bảo dưỡng",       "MAINTENANCE" -> StatusVehicle.MAINTENANCE;
            case "Đặt trước",       "RESERVED"    -> StatusVehicle.RESERVED;
            case "Ngừng hoạt động", "INACTIVE"    -> StatusVehicle.INACTIVE;
            default -> null;
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
    // 6. THỐNG KÊ
    // ==========================================
    public int getTotalActiveCars() { return vehicleDAO.getTotalActiveCars(); }
    public int getRentedCars()      { return vehicleDAO.getRentedCars(); }
    public int getAvailableCars()   { return vehicleDAO.getAvailableCars(); }

    public boolean deleteVehicle(int id) {
        if (!AppSession.isAdmin())
            throw new IllegalStateException("Chỉ Admin mới có quyền xóa xe!");
        return vehicleDAO.delete(id);
    }
}