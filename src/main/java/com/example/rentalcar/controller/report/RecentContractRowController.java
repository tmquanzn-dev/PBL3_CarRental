package com.example.rentalcar.controller.report;

import com.example.rentalcar.bll.StaffReportBLL;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import java.util.Map;

public class RecentContractRowController {

    @FXML private HBox rowContainer;
    @FXML private Label lblCodeContract;
    @FXML private Label lblCustomerName;
    @FXML private Label lblVehicleInfo;
    @FXML private Label lblStatus;
    @FXML private Label lblTotalPrice;

    public void setRowData(Map<String, Object> row) {
        if (row == null) return;

        lblCodeContract.setText(String.valueOf(row.get("code_contract")));
        lblCustomerName.setText(String.valueOf(row.get("customer_name")));
        lblVehicleInfo.setText(row.get("vehicle") + " · " + row.get("code_vehicle"));

        double price = (double) row.get("total_price");
        lblTotalPrice.setText(StaffReportBLL.formatMoneyFull(price));

        String statusStr = String.valueOf(row.get("status"));
        mapStatusAndStyle(statusStr);
    }

    private void mapStatusAndStyle(String s) {
        if (s == null) {
            lblStatus.setText("--");
            lblStatus.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#64748b;");
            return;
        }

        switch (s.replace(" ", "_")) {
            case "DANG_THUE" -> {
                lblStatus.setText("Đang thuê");
                lblStatus.setStyle("-fx-background-color:#dbeafe;-fx-text-fill:#1d4ed8;");
            }
            case "HOAN_THANH" -> {
                lblStatus.setText("Hoàn thành");
                lblStatus.setStyle("-fx-background-color:#dcfce7;-fx-text-fill:#15803d;");
            }
            case "QUA_HAN" -> {
                lblStatus.setText("Quá hạn");
                lblStatus.setStyle("-fx-background-color:#fef3c7;-fx-text-fill:#b45309;");
            }
            case "DA_HUY" -> {
                lblStatus.setText("Đã hủy");
                lblStatus.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#64748b;");
            }
            default -> {
                lblStatus.setText(s);
                lblStatus.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#64748b;");
            }
        }
    }
}