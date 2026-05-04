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

public class AddVehicleController {

    @FXML private TextField txtCode, txtBrand, txtModel, txtColor, txtYear;
    @FXML private TextField txtPriceDay, txtPriceHour, txtCurrentKm,
            txtMaintenanceKm, txtPurchasePrice;
    @FXML private ComboBox<String> cbType;

    // UI upload ảnh
    @FXML private ImageView imgVehiclePreview;
    @FXML private Label     lblImagePath;
    @FXML private Button    btnUploadImage;

    private final VehicleBLL vehicleBLL = new VehicleBLL();
    private String savedImagePath = null; // đường dẫn relative, lưu vào DB

    @FXML
    public void initialize() {
        cbType.getItems().addAll("Tay ga", "Xe số", "Xe côn");

        // Load ảnh mặc định
        ImageHelper.loadDefault(imgVehiclePreview, "/image/dashboardform/card-moto.png");
    }

    //  UPLOAD ẢNH XE
    @FXML
    void handleUploadVehicleImage() {
        Stage stage = (Stage) btnUploadImage.getScene().getWindow();
        String path = ImageHelper.chooseAndSave(stage, ImageHelper.Category.VEHICLE);

        if (path != null) {
            savedImagePath = path;
            ImageHelper.loadInto(imgVehiclePreview, path);
            if (lblImagePath != null) {
                String[] parts = path.replace("\\", "/").split("/");
                lblImagePath.setText("✅  " + parts[parts.length - 1]);
                lblImagePath.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12px;");
            }
        }
    }

    //  LƯU XE
    @FXML
    void handleSave() {
        try {
            if (cbType.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                        "Vui lòng chọn Loại xe (Tay ga / Xe số / Xe côn)!");
                return;
            }

            Vehicles v = new Vehicles();
            v.setCode_vehicle(txtCode.getText());
            v.setBrand(txtBrand.getText());
            v.setModel(txtModel.getText());
            v.setVehicle_type(cbType.getValue());
            v.setColor(txtColor.getText());
            v.setYear_of_manufacture(parseIntegerSafe(txtYear.getText()));
            v.setPrice_day(parseDoubleSafe(txtPriceDay.getText()));
            v.setPrice_hour(parseDoubleSafe(txtPriceHour.getText()));
            v.setCurrent_km(parseIntegerSafe(txtCurrentKm.getText()));
            v.setMaintenance_km(parseIntegerSafe(txtMaintenanceKm.getText()));
            v.setPurchase_price(parseDoubleSafe(txtPurchasePrice.getText()));

            v.setImage_url(savedImagePath != null
                    ? savedImagePath
                    : "/image/dashboardform/card-moto.png");

            v.setStatus(StatusVehicle.AVAILABLE);

            if (vehicleBLL.addVehicle(v)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Đã thêm xe mới vào hệ thống!");
                handleCancel();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi Database",
                        "Không thể lưu vào cơ sở dữ liệu. Vui lòng kiểm tra lại.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu",
                    "Vui lòng kiểm tra lại các trường số!\nLỗi: " + e.getMessage());
        }
    }

    @FXML
    void handleCancel() {
        Stage stage = (Stage) txtCode.getScene().getWindow();
        stage.close();
    }

    //  HELPERS
    private int parseIntegerSafe(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        return Integer.parseInt(text.trim());
    }

    private double parseDoubleSafe(String text) {
        if (text == null || text.trim().isEmpty()) return 0.0;
        return Double.parseDouble(text.trim());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}