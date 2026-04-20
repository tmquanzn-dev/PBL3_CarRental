package com.example.rentalcar.controller.vehicle;

import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.models.Vehicles;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddVehicleController {
    @FXML private TextField txtCode, txtBrand, txtModel, txtColor, txtYear;
    @FXML private TextField txtPriceDay, txtPriceHour, txtCurrentKm, txtMaintenanceKm, txtPurchasePrice, txtImageUrl;
    @FXML private ComboBox<String> cbType;

    private final VehicleBLL vehicleBLL = new VehicleBLL();

    @FXML
    public void initialize() {
        cbType.getItems().addAll("Tay ga", "Xe số", "Xe côn");
    }

    // ==========================================
    // 2 HÀM ÉP KIỂU AN TOÀN (CHỐNG CRASH)
    // ==========================================
    private int parseIntegerSafe(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        return Integer.parseInt(text.trim());
    }

    private double parseDoubleSafe(String text) {
        if (text == null || text.trim().isEmpty()) return 0.0;
        return Double.parseDouble(text.trim());
    }

    @FXML
    void handleSave() {
        try {
            // Rào lỗi: Bắt buộc phải chọn Loại xe
            if (cbType.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn Loại xe (Tay ga / Xe số / Xe côn)!");
                return;
            }

            // Tạo đối tượng từ dữ liệu nhập vào
            Vehicles v = new Vehicles();
            v.setCode_vehicle(txtCode.getText());
            v.setBrand(txtBrand.getText());
            v.setModel(txtModel.getText());
            v.setVehicle_type(cbType.getValue());
            v.setColor(txtColor.getText());

            // SỬ DỤNG HÀM AN TOÀN ĐỂ LẤY SỐ
            v.setYear_of_manufacture(parseIntegerSafe(txtYear.getText()));
            v.setPrice_day(parseDoubleSafe(txtPriceDay.getText()));
            v.setPrice_hour(parseDoubleSafe(txtPriceHour.getText()));
            v.setCurrent_km(parseIntegerSafe(txtCurrentKm.getText()));
            v.setMaintenance_km(parseIntegerSafe(txtMaintenanceKm.getText()));
            v.setPurchase_price(parseDoubleSafe(txtPurchasePrice.getText()));

            v.setImage_url(txtImageUrl.getText());
            v.setStatus(StatusVehicle.AVAILABLE); // Mặc định xe mới là sẵn sàng

            // Gọi BLL để lưu vào DB
            if (vehicleBLL.addVehicle(v)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm xe mới vào hệ thống");
                handleCancel(); // Tự động đóng Popup sau khi lưu thành công
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Không thể lưu vào cơ sở dữ liệu. Vui lòng kiểm tra lại DAO.");
            }

        } catch (Exception e) {
            e.printStackTrace(); // In lỗi đỏ ra console để dễ bắt bệnh nếu còn
            showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu", "Vui lòng kiểm tra lại các trường số! Lỗi chi tiết: " + e.getMessage());
        }
    }

    @FXML
    void handleCancel() {
        // Lấy Stage hiện tại và đóng nó lại
        Stage stage = (Stage) txtCode.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}