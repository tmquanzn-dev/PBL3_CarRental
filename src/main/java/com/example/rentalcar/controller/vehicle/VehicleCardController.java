package com.example.rentalcar.controller.vehicle;

import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.models.Vehicles;
import com.example.rentalcar.utils.ImageHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.text.NumberFormat;
import java.util.Locale;

public class VehicleCardController {

    @FXML private ImageView imgVehicle;
    @FXML private Label     lblPlate, lblStatus, lblName, lblPrice;

    private Vehicles currentVehicle;
    private final NumberFormat formatVND = NumberFormat.getInstance(new Locale("vi", "VN"));

    public void setData(Vehicles vehicles) {
        this.currentVehicle = vehicles;
        if (vehicles == null) return;

        lblPlate.setText(vehicles.getCode_vehicle());
        lblName.setText(vehicles.getBrand() + " " + vehicles.getModel());
        lblPrice.setText(formatVND.format((long) vehicles.getPrice_day()) + " đ");

        // Badge trạng thái
        StatusVehicle status = vehicles.getStatus();
        String statusText = switch (status) {
            case AVAILABLE   -> "Sẵn sàng";
            case RENTED      -> "Đang thuê";
            case MAINTENANCE -> "Bảo dưỡng";
            case RESERVED    -> "Đặt trước";
            default          -> status.name();
        };
        lblStatus.setText(statusText);

        lblStatus.getStyleClass().removeAll("badge-available", "badge-rented",
                "badge-maintenance", "badge-reserved");
        switch (status) {
            case AVAILABLE   -> lblStatus.getStyleClass().add("badge-available");
            case RENTED      -> lblStatus.getStyleClass().add("badge-rented");
            case MAINTENANCE -> lblStatus.getStyleClass().add("badge-maintenance");
            default          -> lblStatus.getStyleClass().add("badge-reserved");
        }

        // ── LOAD ẢNH (dùng ImageHelper để xử lý cả classpath lẫn file system) ──
        String imgUrl = vehicles.getImage_url();

        if (imgUrl == null || imgUrl.isBlank()) {
            // Không có ảnh → dùng ảnh mặc định trong resources
            ImageHelper.loadDefault(imgVehicle, "/image/dashboardform/card-moto.png");
        } else if (imgUrl.startsWith("/") || imgUrl.startsWith("classpath:")) {
            // Đường dẫn classpath (ảnh mặc định cũ)
            ImageHelper.loadDefault(imgVehicle, imgUrl.replace("classpath:", ""));
        } else {
            // Đường dẫn relative lưu từ ImageHelper (uploads/vehicles/...)
            ImageHelper.loadInto(imgVehicle, imgUrl);
        }
    }

    @FXML
    void handleEdit() {
        System.out.println("Edit xe: " + (currentVehicle != null ? currentVehicle.getCode_vehicle() : ""));
        // TODO: mở form Edit xe
    }

    @FXML
    void handleDelete() {
        System.out.println("Yêu cầu xóa xe: " + (currentVehicle != null ? currentVehicle.getCode_vehicle() : ""));
        // TODO: xác nhận và xóa xe
    }
}