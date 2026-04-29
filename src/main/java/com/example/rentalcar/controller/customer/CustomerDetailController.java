package com.example.rentalcar.controller.customer;

import com.example.rentalcar.models.Customers;
import com.example.rentalcar.utils.ImageHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class CustomerDetailController {

    @FXML private Label lblFullName, lblPhone, lblCccd, lblAddress, lblEmail;
    @FXML private Label lblRentalCount, lblStatus;
    @FXML private ImageView imgFront, imgBack;

    public void setCustomerData(Customers customer) {
        if (customer == null) return;

        lblFullName.setText(customer.getFull_name());
        lblPhone.setText(customer.getPhone() != null ? customer.getPhone() : "Chưa cập nhật");
        lblCccd.setText(customer.getCccd());
        lblAddress.setText(customer.getAddress() != null && !customer.getAddress().isEmpty()
                ? customer.getAddress() : "Chưa cập nhật");
        lblEmail.setText(customer.getEmail() != null && !customer.getEmail().isEmpty()
                ? customer.getEmail() : "Chưa cập nhật");
        lblRentalCount.setText(customer.getRental_count() + " lần");

        if (customer.isIs_blacklist()) {
            lblStatus.setText("BLACKLIST");
            lblStatus.setStyle("-fx-background-color: #FCE8E6; -fx-text-fill: #D93025;" +
                    " -fx-padding: 5 12; -fx-background-radius: 12;");
        } else {
            lblStatus.setText("BÌNH THƯỜNG");
            lblStatus.setStyle("-fx-background-color: #E6F4EA; -fx-text-fill: #137333;" +
                    " -fx-padding: 5 12; -fx-background-radius: 12;");
        }

        // ✅ Load ảnh CCCD từ đường dẫn lưu trong DB (dạng "front|back")
        loadCccdImages(customer.getCccd_images());
    }

    private void loadCccdImages(String cccdImages) {
        if (cccdImages == null || cccdImages.isBlank()) return;

        String[] parts = cccdImages.split("\\|", -1);
        String front = parts.length > 0 ? parts[0].trim() : "";
        String back  = parts.length > 1 ? parts[1].trim() : "";

        if (!front.isEmpty()) ImageHelper.loadInto(imgFront, front);
        if (!back.isEmpty())  ImageHelper.loadInto(imgBack,  back);
    }

    @FXML
    void handleClose(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}