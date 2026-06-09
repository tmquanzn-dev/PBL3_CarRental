package com.example.rentalcar.controller.vehicle;

import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.models.Vehicles;
import com.example.rentalcar.utils.ImageHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class EditVehicleController {

    @FXML private TextField txtCode;          // Biển số – chỉ đọc (không cho đổi)
    @FXML private TextField txtBrand;
    @FXML private TextField txtModel;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextField txtColor;
    @FXML private TextField txtYear;
    @FXML private TextField txtPriceDay;
    @FXML private TextField txtPriceHour;
    @FXML private TextField txtCurrentKm;
    @FXML private TextField txtMaintenanceKm;
    @FXML private TextField txtPurchasePrice;
    @FXML private TextField txtFuelCapacity;

    @FXML private ImageView imgVehiclePreview;
    @FXML private Label lblImagePath;
    @FXML private Button btnUploadImage;
    @FXML private Label lblVehicleId;

    private Vehicles currentVehicle;
    private Runnable onSaved;
    private String newImagePath = null;

    private final VehicleBLL vehicleBLL = new VehicleBLL();

    @FXML
    public void initialize() {
        cbType.getItems().addAll("Tay ga", "Xe số", "Xe côn");

        cbStatus.getItems().addAll(
                "Sẵn sàng",
                "Đang thuê",
                "Bảo dưỡng",
                "Đặt trước",
                "Ngừng hoạt động"
        );
    }

    public void setVehicle(Vehicles vehicle) {
        this.currentVehicle = vehicle;
        fillForm(vehicle);
    }

    public void setOnSaved(Runnable callback) {
        this.onSaved = callback;
    }


    private void fillForm(Vehicles v) {
        if (lblVehicleId != null)
            lblVehicleId.setText("ID: " + v.getId_vehicle());

        txtCode.setText(v.getCode_vehicle() != null ? v.getCode_vehicle() : "");
        txtCode.setDisable(true); // Không cho đổi biển số

        txtBrand.setText(v.getBrand() != null ? v.getBrand() : "");
        txtModel.setText(v.getModel() != null ? v.getModel() : "");
        txtColor.setText(v.getColor() != null ? v.getColor() : "");
        txtYear.setText(String.valueOf(v.getYear_of_manufacture()));
        txtPriceDay.setText(String.valueOf((long) v.getPrice_day()));
        txtPriceHour.setText(String.valueOf((long) v.getPrice_hour()));
        txtCurrentKm.setText(String.valueOf(v.getCurrent_km()));
        txtMaintenanceKm.setText(String.valueOf(v.getMaintenance_km()));
        txtPurchasePrice.setText(String.valueOf((long) v.getPurchase_price()));
        txtFuelCapacity.setText(String.valueOf(v.getFuel_capacity()));

        // ComboBox loại xe
        if (v.getVehicle_type() != null)
            cbType.setValue(v.getVehicle_type());

        // ComboBox trạng thái
        if (v.getStatus() != null) {
            cbStatus.setValue(statusToDisplay(v.getStatus()));
        }

        // Load ảnh hiện tại
        String imgUrl = v.getImage_url();
        if (imgUrl != null && !imgUrl.isBlank()) {
            if (imgUrl.startsWith("/")) {
                ImageHelper.loadDefault(imgVehiclePreview, imgUrl);
            } else {
                ImageHelper.loadInto(imgVehiclePreview, imgUrl);
            }
            if (lblImagePath != null) {
                String[] parts = imgUrl.replace("\\", "/").split("/");
                lblImagePath.setText("✅  " + parts[parts.length - 1]);
                lblImagePath.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12px;");
                lblImagePath.setVisible(true);
                lblImagePath.setManaged(true);
            }
        } else {
            ImageHelper.loadDefault(imgVehiclePreview, "/icon/dashboardform/card-moto.png");
        }
    }

    @FXML
    void handleUploadVehicleImage() {
        Stage stage = (Stage) btnUploadImage.getScene().getWindow();
        String path = ImageHelper.chooseAndSave(stage, ImageHelper.Category.VEHICLE);

        if (path != null) {
            newImagePath = path;
            ImageHelper.loadInto(imgVehiclePreview, path);
            if (lblImagePath != null) {
                String[] parts = path.replace("\\", "/").split("/");
                lblImagePath.setText("✅  " + parts[parts.length - 1]);
                lblImagePath.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12px;");
                lblImagePath.setVisible(true);
                lblImagePath.setManaged(true);
            }
        }
    }

    @FXML
    void handleSave() {
        try {
            // Validate
            if (cbType.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                        "Vui lòng chọn Loại xe!");
                return;
            }
            if (cbStatus.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                        "Vui lòng chọn Trạng thái!");
                return;
            }

            // Gán dữ liệu mới vào object xe hiện tại
            currentVehicle.setBrand(txtBrand.getText().trim());
            currentVehicle.setModel(txtModel.getText().trim());
            currentVehicle.setVehicle_type(cbType.getValue());
            currentVehicle.setColor(txtColor.getText().trim());
            currentVehicle.setYear_of_manufacture(parseIntSafe(txtYear.getText()));
            currentVehicle.setPrice_day(parseDoubleSafe(txtPriceDay.getText()));
            currentVehicle.setPrice_hour(parseDoubleSafe(txtPriceHour.getText()));
            currentVehicle.setCurrent_km(parseIntSafe(txtCurrentKm.getText()));
            currentVehicle.setMaintenance_km(parseIntSafe(txtMaintenanceKm.getText()));
            currentVehicle.setPurchase_price(parseDoubleSafe(txtPurchasePrice.getText()));
            currentVehicle.setFuel_capacity(parseIntSafe(txtFuelCapacity.getText()));
            currentVehicle.setStatus(displayToStatus(cbStatus.getValue()));

            // Cập nhật ảnh nếu đã chọn ảnh mới
            if (newImagePath != null) {
                currentVehicle.setImage_url(newImagePath);
            }

            boolean ok = vehicleBLL.updateVehicle(currentVehicle);

            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Đã cập nhật thông tin xe " + currentVehicle.getCode_vehicle() + " thành công!");
                if (onSaved != null) onSaved.run();
                closeStage();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi",
                        "Không thể cập nhật. Vui lòng thử lại!");
            }

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống",
                    "Lỗi: " + e.getMessage());
        }
    }

    @FXML
    void handleCancel() {
        closeStage();
    }

    // ── Helpers ───────────────────────────────────────────────────
    private void closeStage() {
        Stage stage = (Stage) txtCode.getScene().getWindow();
        stage.close();
    }

    private int parseIntSafe(String text) {
        try {
            return Integer.parseInt(text.trim().replace(",", "").replace(".", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDoubleSafe(String text) {
        try {
            return Double.parseDouble(text.trim().replace(",", "").replace(".", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String statusToDisplay(StatusVehicle s) {
        return switch (s) {
            case AVAILABLE   -> "Sẵn sàng";
            case RENTED      -> "Đang thuê";
            case MAINTENANCE -> "Bảo dưỡng";
            case RESERVED    -> "Đặt trước";
            default          -> "Ngừng hoạt động";
        };
    }

    private StatusVehicle displayToStatus(String display) {
        return switch (display) {
            case "Sẵn sàng"         -> StatusVehicle.AVAILABLE;
            case "Đang thuê"        -> StatusVehicle.RENTED;
            case "Bảo dưỡng"        -> StatusVehicle.MAINTENANCE;
            case "Đặt trước"        -> StatusVehicle.RESERVED;
            default                  -> StatusVehicle.INACTIVE;
        };
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}