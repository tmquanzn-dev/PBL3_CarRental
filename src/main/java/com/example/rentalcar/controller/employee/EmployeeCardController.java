package com.example.rentalcar.controller.employee;

import com.example.rentalcar.models.Users;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

public class EmployeeCardController {
    @FXML private Label lblFullName, lblRoleName, lblUsername, lblOrderCount, lblStatus, lblInitial;
    @FXML private Circle circleAvatar;

    public void setData(Users user) {
        if (lblFullName == null || lblRoleName == null || lblUsername == null) {
            System.err.println("LỖI: fx:id chưa khớp với Controller!");
            return;
        }

        lblFullName.setText(user.getFull_name());
        lblRoleName.setText(user.getRole_name() != null ? user.getRole_name() : "Nhân viên");
        lblUsername.setText("@" + user.getUsername());
        lblOrderCount.setText("0 đơn"); // Sau này join với bảng Orders để show số đơn nhân viên tạo được

        if (user.getFull_name() != null && !user.getFull_name().isEmpty())
            lblInitial.setText(user.getFull_name().substring(0, 1).toUpperCase());

        lblStatus.getStyleClass().removeAll("status-active", "status-locked");
        if (user.isIs_active()) {
            lblStatus.setText("Hoạt động");
            lblStatus.getStyleClass().add("status-active");
        } else {
            lblStatus.setText("Đã khóa");
            lblStatus.getStyleClass().add("status-locked");
        }
    }

    @FXML
    void handleEdit()
    {
        System.out.println("Sửa nhanaviene: ");
    }


    @FXML void handleLock()
    {
        System.out.println("...");
    }
}
