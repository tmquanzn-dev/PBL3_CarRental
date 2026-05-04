package com.example.rentalcar.controller.vehicle;

import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.models.Vehicles;
import com.example.rentalcar.utils.AppSession;
import com.example.rentalcar.utils.ImageHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

public class VehicleCardController {

    @FXML private ImageView imgVehicle;
    @FXML private Label     lblPlate, lblStatus, lblName, lblPrice;
    @FXML private Button    btnEdit;
    @FXML private Button    btnDelete;

    private Vehicles currentVehicle;
    private Runnable onRefresh;

    private final VehicleBLL vehicleBLL = new VehicleBLL();
    private final NumberFormat fmtVND = NumberFormat.getInstance(new Locale("vi", "VN"));

    public void setData(Vehicles vehicle) {
        this.currentVehicle = vehicle;
        if (vehicle == null) return;

        lblPlate.setText(vehicle.getCode_vehicle());
        lblName.setText(vehicle.getBrand() + " " + vehicle.getModel());
        lblPrice.setText(fmtVND.format((long) vehicle.getPrice_day()) + " đ");

        //Badge trạng thái
        StatusVehicle st = vehicle.getStatus();
        String statusText = VehicleBLL.statusToDisplay(st);
        lblStatus.setText(statusText);
        //
        lblStatus.getStyleClass().removeAll(
                "badge-available", "badge-rented",
                "badge-maintenance", "badge-reserved", "badge-inactive"
        );
        switch (st) {
            case AVAILABLE   -> lblStatus.getStyleClass().add("badge-available");
            case RENTED      -> lblStatus.getStyleClass().add("badge-rented");
            case MAINTENANCE -> lblStatus.getStyleClass().add("badge-maintenance");
            case RESERVED    -> lblStatus.getStyleClass().add("badge-reserved");
            case INACTIVE    -> lblStatus.getStyleClass().add("badge-inactive");
        }

        //Làm mờ card xe INACTIVE
        if (st == StatusVehicle.INACTIVE) {
            lblPlate.setStyle("-fx-text-fill: #94a3b8;");
            lblName.setStyle("-fx-text-fill: #94a3b8;");
            lblPrice.setStyle("-fx-text-fill: #94a3b8;");
        }

        //Load ảnh
        String imgUrl = vehicle.getImage_url();
        if (imgUrl == null || imgUrl.isBlank()) {
            ImageHelper.loadDefault(imgVehicle, "/image/dashboardform/card-moto.png");
        } else if (imgUrl.startsWith("/") || imgUrl.startsWith("classpath:")) {
            ImageHelper.loadDefault(imgVehicle, imgUrl.replace("classpath:", ""));
        } else {
            ImageHelper.loadInto(imgVehicle, imgUrl);
        }
        // Nút Xóa: chỉ Admin, và chỉ hiện với xe ĐANG HOẠT ĐỘNG
        if (btnDelete != null) {
            boolean canDelete = AppSession.isAdmin() && st != StatusVehicle.INACTIVE;
            btnDelete.setVisible(canDelete);
            btnDelete.setManaged(canDelete);

            // Nếu xe INACTIVE thì đổi nút Delete thành khôi phục
            if (AppSession.isAdmin() && st == StatusVehicle.INACTIVE) {
                btnDelete.setText("♻️ Khôi phục");
                btnDelete.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-cursor: hand;");
                btnDelete.setVisible(true);
                btnDelete.setManaged(true);
            }
        }
        if (btnEdit != null) {
            boolean canEdit = AppSession.isAdmin() ||
                    (AppSession.isStaff() && st != StatusVehicle.INACTIVE);
            btnEdit.setVisible(canEdit);
            btnEdit.setManaged(canEdit);
        }
    }

    public void setOnRefresh(Runnable callback) {
        this.onRefresh = callback;
    }

    //Sửa xe
    @FXML
    void handleEdit() {
        if (currentVehicle == null)
            return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/vehicle/EditVehicleView.fxml"));
            Parent root = loader.load();

            EditVehicleController ctrl = loader.getController();
            ctrl.setVehicle(currentVehicle);
            ctrl.setOnSaved(() -> {
                if (onRefresh != null) onRefresh.run();
            });

            Stage stage = new Stage();
            stage.setTitle("Chỉnh sửa xe: " + currentVehicle.getCode_vehicle());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();

        } catch (Exception e) {
            System.err.println("Lỗi mở form sửa xe: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Không thể mở form sửa xe: " + e.getMessage());
        }
    }

    //Xóa xe hoặc Khôi phục xe
    @FXML
    void handleDelete() {
        if (!AppSession.isAdmin() || currentVehicle == null) return;

        StatusVehicle st = currentVehicle.getStatus();

        if (st == StatusVehicle.INACTIVE) {
            //Khôi phục xe đã ngừng hoạt động
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Khôi phục xe");
            confirm.setHeaderText(null);
            confirm.setContentText("Khôi phục xe " + currentVehicle.getCode_vehicle()
                    + " về trạng thái Sẵn sàng?");
            confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                currentVehicle.setStatus(StatusVehicle.AVAILABLE);
                boolean ok = vehicleBLL.updateVehicle(currentVehicle);
                if (ok) {
                    if (onRefresh != null) onRefresh.run();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Không thể khôi phục xe này!");
                }
            });

        } else {
            //Xóa mềm (chuyển sang INACTIVE)
            if (st == StatusVehicle.RENTED) {
                showAlert(Alert.AlertType.WARNING,
                        "Không thể xóa xe đang cho thuê!\nVui lòng hoàn thành hợp đồng trước.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận xóa xe");
            confirm.setHeaderText(null);
            confirm.setContentText("Xóa xe " + currentVehicle.getCode_vehicle()
                    + "?\nXe sẽ chuyển sang Ngừng hoạt động.\nBạn có thể khôi phục lại sau.");
            confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                boolean ok = vehicleBLL.deleteVehicle(currentVehicle.getId_vehicle());
                if (ok) {
                    if (onRefresh != null) onRefresh.run();
                } else {
                    showAlert(Alert.AlertType.ERROR,
                            "Không thể xóa xe này! Xe có thể đang có hợp đồng liên quan.");
                }
            });
        }
    }

    //Helper
    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Lỗi" : "Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}