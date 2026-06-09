package com.example.rentalcar.controller.settings;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

public class NotificationPanelController {
    @FXML private Button toggleNotiNewContract, toggleNotiOverdue, toggleNotiMaint, toggleNotiVoucher;

    private boolean notiNew = true, notiOverdue = true, notiMaint = true, notiVoucher = false;

    @FXML void handleToggleNotiNewContract() {
        notiNew = !notiNew;
        updateToggle(toggleNotiNewContract, notiNew);
    }

    @FXML void handleToggleNotiOverdue() {
        notiOverdue = !notiOverdue;
        updateToggle(toggleNotiOverdue, notiOverdue);
    }

    @FXML void handleToggleNotiMaint() {
        notiMaint = !notiMaint;
        updateToggle(toggleNotiMaint, notiMaint);
    }

    @FXML void handleToggleNotiVoucher() {
        notiVoucher = !notiVoucher;
        updateToggle(toggleNotiVoucher, notiVoucher);
    }

    @FXML void handleNotiSave() {
        new Alert(Alert.AlertType.INFORMATION, "✅  Đã lưu cấu hình thông báo!").showAndWait();
    }

    private void updateToggle(Button btn, boolean isOn) {
        btn.setText(isOn ? "BẬT" : "TẮT");
        btn.getStyleClass().removeAll("toggle-btn-on", "toggle-btn-off");
        btn.getStyleClass().add(isOn ? "toggle-btn-on" : "toggle-btn-off");
    }
}