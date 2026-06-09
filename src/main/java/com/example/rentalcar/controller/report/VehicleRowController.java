package com.example.rentalcar.controller.report;

import com.example.rentalcar.models.VehicleReportRow;
import com.example.rentalcar.bll.ReportBLL;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class VehicleRowController {

    @FXML private HBox rowContainer;
    @FXML private Label lblRank;
    @FXML private Label lblVehicleName;
    @FXML private Label lblPlateNumber;
    @FXML private Label lblRentalCount;
    @FXML private Label lblRevenue;

    public void setRowData(VehicleReportRow row) {
        if (row == null) return;

        lblRank.setText(String.valueOf(row.getRank()));
        lblVehicleName.setText(row.getVehicleName() != null ? row.getVehicleName() : "Chưa xác định");
        lblPlateNumber.setText(row.getPlateNumber() != null ? row.getPlateNumber() : "--");
        lblRentalCount.setText(row.getRentalCount() + " lượt");
        lblRevenue.setText(ReportBLL.formatMoneyFull(row.getRevenue()));

        if (row.getRank() % 2 == 0) {
            rowContainer.setStyle("-fx-background-color: #f8fafc;");
        } else {
            rowContainer.setStyle("-fx-background-color: #ffffff;");
        }

        // 3. Định dạng màu chữ số hạng theo danh hiệu huy chương (Gia tăng trải nghiệm thị giác)
        switch (row.getRank()) {
            case 1 -> lblRank.setStyle("-fx-text-fill: #b45309; -fx-font-weight: bold; -fx-font-size: 13px;"); // Top 1: Vàng đồng
            case 2 -> lblRank.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 13px;"); // Top 2: Bạc khói
            case 3 -> lblRank.setStyle("-fx-text-fill: #be185d; -fx-font-weight: bold; -fx-font-size: 13px;"); // Top 3: Đồng đỏ
            default -> lblRank.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");                      // Còn lại: Xám mờ
        }
    }
}