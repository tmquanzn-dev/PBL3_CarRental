package com.example.rentalcar.controller.customer;

import com.example.rentalcar.models.Customers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class CustomerDetailController {

    // Đã sửa đồng bộ 100% thành Label để khớp với file FXML
    @FXML private Label lblFullName;
    @FXML private Label lblPhone;
    @FXML private Label lblCccd;
    @FXML private Label lblAddress;
    @FXML private Label lblEmail;
    @FXML private Label lblRentalCount;
    @FXML private Label lblStatus;

    @FXML private ImageView imgFront;
    @FXML private ImageView imgBack;

    public void setCustomerData(Customers customer) {
        if (customer == null) return;

        lblFullName.setText(customer.getFull_name());
        lblPhone.setText(customer.getPhone());
        lblCccd.setText(customer.getCccd());
        lblAddress.setText(customer.getAddress() != null && !customer.getAddress().isEmpty() ? customer.getAddress() : "Chưa cập nhật");
        lblEmail.setText(customer.getEmail() != null && !customer.getEmail().isEmpty() ? customer.getEmail() : "Chưa cập nhật");

        lblRentalCount.setText(customer.getRental_count() + " lần");

        if (customer.isIs_blacklist()) {
            lblStatus.setText("BLACKLIST");
            lblStatus.setStyle("-fx-background-color: #FCE8E6; -fx-text-fill: #D93025; -fx-padding: 5 12; -fx-background-radius: 12;");
        } else {
            lblStatus.setText("BÌNH THƯỜNG");
            lblStatus.setStyle("-fx-background-color: #E6F4EA; -fx-text-fill: #137333; -fx-padding: 5 12; -fx-background-radius: 12;");
        }

        // TODO: Xử lý set Image hiển thị CCCD ở đây nếu bạn đã có ảnh trong Database
    }

    @FXML
    void handleClose(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}