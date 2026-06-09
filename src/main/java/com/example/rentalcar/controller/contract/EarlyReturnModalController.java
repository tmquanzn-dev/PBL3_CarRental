package com.example.rentalcar.controller.contract;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class EarlyReturnModalController {

    @FXML private Label lblTimeUnused;
    @FXML private Label lblActualUsedPrice;
    @FXML private Label lblPenaltyFee;
    @FXML private Label lblNewBasePrice;

    private boolean confirmed = false;


    public void setData(String timeUnusedStr, double actualUsed, double penalty, double newBase) {
        if (lblTimeUnused != null) lblTimeUnused.setText("Thời gian hoàn thành sớm: " + timeUnusedStr);
        if (lblActualUsedPrice != null) lblActualUsedPrice.setText(fmt(actualUsed));
        if (lblPenaltyFee != null) lblPenaltyFee.setText("+ " + fmt(penalty));
        if (lblNewBasePrice != null) lblNewBasePrice.setText(fmt(newBase));
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    private void handleConfirm() {
        this.confirmed = true;
        closeStage();
    }

    @FXML
    private void handleCancel() {
        this.confirmed = false;
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) lblTimeUnused.getScene().getWindow();
        stage.close();
    }

    private String fmt(double v) {
        return String.format("%,.0f đ", v).replace(",", ".");
    }
}