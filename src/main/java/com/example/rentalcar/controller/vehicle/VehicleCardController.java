package com.example.rentalcar.controller.vehicle;

import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.models.Vehicles;
import com.example.rentalcar.utils.AppSession;
import com.example.rentalcar.utils.ImageHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.text.NumberFormat;
import java.util.Locale;

public class VehicleCardController {

    @FXML private ImageView imgVehicle;
    @FXML private Label     lblPlate, lblStatus, lblName, lblPrice;
    @FXML private Button    btnEdit;    // fx:id="btnEdit"   trong VehicleCard.fxml
    @FXML private Button    btnDelete;  // fx:id="btnDelete" trong VehicleCard.fxml

    private Vehicles currentVehicle;
    private Runnable onRefresh;   // callback để VehicleController reload sau khi sửa/xóa

    private final VehicleBLL vehicleBLL = new VehicleBLL();
    private final NumberFormat fmtVND = NumberFormat.getInstance(new Locale("vi", "VN"));

    // ── Gọi từ VehicleController sau khi load card ────────────
    public void setData(Vehicles vehicle) {
        this.currentVehicle = vehicle;
        if (vehicle == null) return;

        lblPlate.setText(vehicle.getCode_vehicle());
        lblName.setText(vehicle.getBrand() + " " + vehicle.getModel());
        lblPrice.setText(fmtVND.format((long) vehicle.getPrice_day()) + " đ");

        // Badge trạng thái
        StatusVehicle st = vehicle.getStatus();
        String statusText = switch (st) {
            case AVAILABLE   -> "Sẵn sàng";
            case RENTED      -> "Đang thuê";
            case MAINTENANCE -> "Bảo dưỡng";
            case RESERVED    -> "Đặt trước";
            default          -> st.name();
        };
        lblStatus.setText(statusText);
        lblStatus.getStyleClass().removeAll("badge-available", "badge-rented",
                "badge-maintenance", "badge-reserved");
        switch (st) {
            case AVAILABLE   -> lblStatus.getStyleClass().add("badge-available");
            case RENTED      -> lblStatus.getStyleClass().add("badge-rented");
            case MAINTENANCE -> lblStatus.getStyleClass().add("badge-maintenance");
            default          -> lblStatus.getStyleClass().add("badge-reserved");
        }

        // ── Load ảnh dùng ImageHelper (xử lý cả classpath lẫn file system) ──
        String imgUrl = vehicle.getImage_url();
        if (imgUrl == null || imgUrl.isBlank()) {
            ImageHelper.loadDefault(imgVehicle, "/image/dashboardform/card-moto.png");
        } else if (imgUrl.startsWith("/") || imgUrl.startsWith("classpath:")) {
            ImageHelper.loadDefault(imgVehicle, imgUrl.replace("classpath:", ""));
        } else {
            // Đường dẫn relative lưu từ ImageHelper (uploads/vehicles/...)
            ImageHelper.loadInto(imgVehicle, imgUrl);
        }

        // ── Phân quyền: Staff không được xóa xe ──────────────
        if (btnDelete != null) {
            btnDelete.setVisible(AppSession.isAdmin());
            btnDelete.setManaged(AppSession.isAdmin());
        }
    }

    public void setOnRefresh(Runnable callback) {
        this.onRefresh = callback;
    }

    // ── Sửa xe ────────────────────────────────────────────────
    @FXML
    void handleEdit() {
        // TODO: mở modal sửa xe, truyền currentVehicle vào
        System.out.println("Edit xe: " + (currentVehicle != null ? currentVehicle.getCode_vehicle() : "null"));
    }

    // ── Xóa xe (Admin only) ───────────────────────────────────
    @FXML
    void handleDelete() {
        if (!AppSession.isAdmin()) return;  // guard thêm
        if (currentVehicle == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa xe");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa xe " + currentVehicle.getCode_vehicle() + "?\n"
                + "Xe sẽ bị chuyển sang trạng thái INACTIVE.");
        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            if (vehicleBLL.deleteVehicle(currentVehicle.getId_vehicle())) {
                if (onRefresh != null) onRefresh.run();
            } else {
                new Alert(Alert.AlertType.ERROR, "Không thể xóa xe này!").showAndWait();
            }
        });
    }
}