// FILE: CustomerEditController.java
package com.example.rentalcar.controller.customer;

import com.example.rentalcar.bll.CustomerBLL;
import com.example.rentalcar.models.Customers;
import javafx.event.ActionEvent; // ĐẢM BẢO DÙNG JAVAFX
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class CustomerEditController {
    @FXML private Label lblCustomerId;
    @FXML private TextField txtName, txtPhone, txtCccd, txtAddress, txtEmail;
    @FXML private ImageView imgFront, imgBack;

    private final CustomerBLL customerBLL = new CustomerBLL();
    private Customers customers;

    public void setCustomerData(Customers customer) {
        if (customer == null) return;
        this.customers = customer;

        lblCustomerId.setText("ID: " + customer.getId_customer());
        txtName.setText(customer.getFull_name());
        txtPhone.setText(customer.getPhone());
        txtCccd.setText(customer.getCccd());
        txtEmail.setText(customer.getEmail());
        txtAddress.setText(customer.getAddress());
    }

    @FXML
    void handleSave(ActionEvent event) {
        customers.setFull_name(txtName.getText());
        customers.setPhone(txtPhone.getText());
        customers.setCccd(txtCccd.getText());
        customers.setEmail(txtEmail.getText());
        customers.setAddress(txtAddress.getText());

        try {
            boolean success = customerBLL.updateCustomer(customers);
            if (success) closeStage(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleClose(ActionEvent event) {
        closeStage(event);
    }

    @FXML
    void handleUploadFront(ActionEvent event) {
        System.out.println("Đang tải ảnh mặt trước lên...");
    }

    @FXML
    void handleUploadBack(ActionEvent event) {
        System.out.println("Đang tải ảnh mặt sau lên...");
    }

    private void closeStage(ActionEvent event) {
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }
}