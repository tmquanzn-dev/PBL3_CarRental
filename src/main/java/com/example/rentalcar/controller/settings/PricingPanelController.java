package com.example.rentalcar.controller.settings;

import com.example.rentalcar.bll.SystemSettingBLL;
import com.example.rentalcar.utils.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PricingPanelController {
    @FXML private TextField txtLatePenalty, txtFuelPrice;
    @FXML private ComboBox<String> cbLateCalcMode, cbFuelMultiplier;
    @FXML private Label lblPreviewLate, lblPreviewFuel;

    private final SystemSettingBLL settingBLL = new SystemSettingBLL();

    public void initPricing() {
        cbLateCalcMode.getItems().addAll("Làm tròn lên theo giờ (mặc định)", "Tính chính xác theo phút");
        cbLateCalcMode.setValue("Làm tròn lên theo giờ (mặc định)");

        cbFuelMultiplier.getItems().addAll("x1.0 (không phụ phí)", "x1.2 (+20% phụ phí)", "x1.5 (+50% phụ phí)");
        cbFuelMultiplier.setValue("x1.0 (không phụ phí)");

        txtLatePenalty.textProperty().addListener((o, old, v) -> updatePricingPreview());
        txtFuelPrice.textProperty().addListener((o, old, v) -> updatePricingPreview());
        loadPricingFromDB();
    }

    public void loadPricingFromDB() {
        try {
            double late = settingBLL.getDoubleSetting("Phi_Tre_Gio", 100000.0);
            double fuel = settingBLL.getDoubleSetting("Gia_Xang_Litre", 25000.0);
            txtLatePenalty.setText(String.valueOf((long) late));
            txtFuelPrice.setText(String.valueOf((long) fuel));
            updatePricingPreview();
        } catch (Exception e) {
            System.err.println("Lỗi load cấu hình giá: " + e.getMessage());
        }
    }

    private void updatePricingPreview() {
        try {
            double late = parseDouble(txtLatePenalty.getText(), 100000);
            double fuel = parseDouble(txtFuelPrice.getText(), 25000);
            lblPreviewLate.setText(String.format("%,.0f đ", late * 2).replace(",", "."));
            lblPreviewFuel.setText(String.format("%,.0f đ", fuel * 5).replace(",", "."));
        } catch (Exception ignored) {}
    }

    @FXML void handlePricingSave() {
        try {
            double late = Double.parseDouble(txtLatePenalty.getText().trim().replace(",", ""));
            double fuel = Double.parseDouble(txtFuelPrice.getText().trim().replace(",", ""));
            int editorId = AppSession.getCurrentUser().getId_user();

            if (settingBLL.updateSettingValue("Phi_Tre_Gio", String.valueOf((long) late), editorId) &
                    settingBLL.updateSettingValue("Gia_Xang_Litre", String.valueOf((long) fuel), editorId)) {
                new Alert(Alert.AlertType.INFORMATION, "✅  Đã cập nhật biểu giá tài chính thành công!").showAndWait();
            }
        } catch (Exception e) { new Alert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage()).showAndWait(); }
    }

    @FXML void handlePricingCancel() { loadPricingFromDB(); }

    private double parseDouble(String text, double def) {
        try {
            return Double.parseDouble(text.trim().replace(",", ""));
        } catch (Exception e) {
            return def;
        }
    }
}