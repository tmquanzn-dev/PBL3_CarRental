package com.example.rentalcar.controller.settings;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class GeneralPanelController {
    @FXML private ComboBox<String> cbLanguage, cbDateFormat, cbCurrency, cbPageSize;
    @FXML private Button toggleAutoOverdue, toggleMaintWarn, toggleConfirmDelete;

    private boolean autoOverdue = true, maintWarn = true, confirmDel = true;

    public void initGeneral() {
        cbLanguage.getItems().addAll("Tiếng Việt","English"); cbLanguage.setValue("Tiếng Việt");
        cbDateFormat.getItems().addAll("dd/MM/yyyy HH:mm","yyyy-MM-dd HH:mm","MM/dd/yyyy hh:mm a"); cbDateFormat.setValue("dd/MM/yyyy HH:mm");
        cbCurrency.getItems().addAll("VNĐ (đ)","USD ($)"); cbCurrency.setValue("VNĐ (đ)");
        cbPageSize.getItems().addAll("10 hàng","20 hàng","50 hàng","100 hàng"); cbPageSize.setValue("20 hàng");
    }

    @FXML void handleToggleAutoOverdue() {
        autoOverdue = !autoOverdue;
        updateToggle(toggleAutoOverdue, autoOverdue);

    }
    @FXML void handleToggleMaintWarn()
    {
        maintWarn = !maintWarn;
        updateToggle(toggleMaintWarn, maintWarn);
    }
    @FXML void handleToggleConfirmDelete(){
        confirmDel = !confirmDel;
        updateToggle(toggleConfirmDelete, confirmDel);
    }

    @FXML void handleRestoreDefaults() {
        cbLanguage.setValue("Tiếng Việt");
        cbDateFormat.setValue("dd/MM/yyyy HH:mm");
        cbCurrency.setValue("VNĐ (đ)");
        cbPageSize.setValue("20 hàng");

        autoOverdue = true; maintWarn = true; confirmDel = true;

        updateToggle(toggleAutoOverdue, true);
        updateToggle(toggleMaintWarn, true);
        updateToggle(toggleConfirmDelete, true);
    }

    @FXML void handleGeneralSave() {
        new Alert(Alert.AlertType.INFORMATION, "✅  Đã lưu cài đặt hệ thống!").showAndWait();
    }

    private void updateToggle(Button btn, boolean isOn) {
        btn.setText(isOn ? "BẬT" : "TẮT");
        btn.getStyleClass().removeAll("toggle-btn-on", "toggle-btn-off");
        btn.getStyleClass().add(isOn ? "toggle-btn-on" : "toggle-btn-off");
    }
}