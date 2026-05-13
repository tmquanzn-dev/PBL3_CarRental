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

    @FXML private ImageView imgVehiclePreview;
    @FXML private Label     lblImagePath;
    @FXML private Button    btnUploadImage;

    private final VehicleBLL vehicleBLL = new VehicleBLL();

    // ⭐ Lưu đường dẫn ABSOLUTE để hiển thị, và RELATIVE để lưu DB
    private String savedImagePathAbsolute = null;
    private String savedImagePathRelative = null;

    @FXML
    public void initialize() {
        cbType.getItems().addAll("Tay ga", "Xe số", "Xe côn");
        // Load ảnh mặc định
        ImageHelper.loadDefault(imgVehiclePreview, "/image/dashboardform/card-moto.png");
    }

    // =========================================================
    //  UPLOAD ẢNH XE
    // =========================================================
    @FXML
    void handleUploadVehicleImage() {
        Stage stage = (Stage) btnUploadImage.getScene().getWindow();
        String relativePath = ImageHelper.chooseAndSave(stage, ImageHelper.Category.VEHICLE);

        if (relativePath != null) {
            savedImagePathRelative = relativePath;
            // Dùng loadInto để hiển thị preview (tự resolve absolute path)
            ImageHelper.loadInto(imgVehiclePreview, relativePath);

            if (lblImagePath != null) {
                // Lấy tên file ngắn để hiển thị
                String[] parts = relativePath.replace("\\", "/").split("/");
                String fileName = parts[parts.length - 1];
                // Cắt ngắn nếu quá dài
                if (fileName.length() > 30) fileName = fileName.substring(0, 30) + "...";
                lblImagePath.setText("✅  " + fileName);
                lblImagePath.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12px; -fx-font-weight: bold;");
                lblImagePath.setVisible(true);
                lblImagePath.setManaged(true);
            }
        }
    }

    // =========================================================
    //  LƯU XE
    // =========================================================
    @FXML
    void handleSave() {
        try {
            // Validate loại xe
            if (cbType.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                        "Vui lòng chọn Loại xe (Tay ga / Xe số / Xe côn)!");
                return;
            }

            // Validate biển số
            String code = txtCode.getText().trim();
            if (code.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập biển số xe!");
                return;
            }

            Vehicles v = new Vehicles();
            v.setCode_vehicle(code);
            v.setBrand(txtBrand.getText().trim());
            v.setModel(txtModel.getText().trim());
            v.setVehicle_type(cbType.getValue());
            v.setColor(txtColor.getText().trim());
            v.setYear_of_manufacture(parseIntegerSafe(txtYear.getText()));
            v.setPrice_day(parseDoubleSafe(txtPriceDay.getText()));
            v.setPrice_hour(parseDoubleSafe(txtPriceHour.getText()));
            v.setFuel_capacity(0); // mặc định, có thể thêm field sau
            v.setCurrent_km(parseIntegerSafe(txtCurrentKm.getText()));
            v.setMaintenance_km(parseIntegerSafe(txtMaintenanceKm.getText()));
            v.setPurchase_price(parseDoubleSafe(txtPurchasePrice.getText()));
            v.setStatus(StatusVehicle.AVAILABLE);

            // ⭐ Lưu đường dẫn ảnh: dùng relative path nếu đã upload, không thì dùng default
            if (savedImagePathRelative != null && !savedImagePathRelative.isBlank()) {
                v.setImage_url(savedImagePathRelative);
            } else {
                v.setImage_url("/image/dashboardform/card-moto.png");
            }

            if (vehicleBLL.addVehicle(v)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Đã thêm xe " + code + " vào hệ thống!");
                handleCancel();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi Database",
                        "Không thể lưu vào cơ sở dữ liệu. Biển số có thể đã tồn tại.");
            }

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nghiệp vụ", e.getMessage());
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

    // =========================================================
    //  HELPERS
    // =========================================================
    private int parseIntegerSafe(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        try { return Integer.parseInt(text.trim().replace(",", "").replace(".", "")); }
        catch (NumberFormatException e) { return 0; }
    }

    private double parseDoubleSafe(String text) {
        if (text == null || text.trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(text.trim().replace(",", "").replace(".", "")); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}