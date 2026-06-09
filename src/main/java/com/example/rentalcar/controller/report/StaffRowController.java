package com.example.rentalcar.controller.report;

import com.example.rentalcar.models.StaffReportRow;
import com.example.rentalcar.bll.ReportBLL;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class StaffRowController {
    @FXML private HBox rowContainer;
    @FXML private Label lblRank;
    @FXML private Label lblAvatar;
    @FXML private Label lblStaffName;
    @FXML private Label lblContractCount;
    @FXML private Label lblRevenue;

    public void setRowData(StaffReportRow row) {
        lblRank.setText(String.valueOf(row.getRank()));

        String name = row.getFullName() != null ? row.getFullName() : "?";
        lblStaffName.setText(name);
        lblAvatar.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());

        lblContractCount.setText(row.getContractCount() + " HĐ");
        lblRevenue.setText(ReportBLL.formatMoneyFull(row.getRevenue()));

        if (row.getRank() % 2 == 0) {
            rowContainer.setStyle("-fx-background-color: #f8fafc;");
        } else {
            rowContainer.setStyle("-fx-background-color: #ffffff;");
        }

        switch (row.getRank()) {
            case 1 -> lblRank.setStyle("-fx-text-fill: #b45309; -fx-font-weight: bold;");
            case 2 -> lblRank.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
            case 3 -> lblRank.setStyle("-fx-text-fill: #be185d; -fx-font-weight: bold;");
            default -> lblRank.setStyle("-fx-text-fill: #94a3b8;");
        }
    }
}