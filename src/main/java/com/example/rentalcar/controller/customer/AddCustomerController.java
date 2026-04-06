package com.example.rentalcar.controller.customer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class AddCustomerController {

    // Khai báo các thành phần giao diện (khớp với fx:id trong FXML)
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtCccd;
    @FXML private TextField txtAddress;
    @FXML private TextField txtEmail;

    @FXML private ImageView imgFront;
    @FXML private ImageView imgBack;

    @FXML
    public void initialize() {
        btnClose.setOnAction(event -> closeModal());
        btnCancel.setOnAction(event -> closeModal());

        //  Xử lý nút Lưu khách hàng
        btnSave.setOnAction(event -> {
            System.out.println("Đang lưu khách hàng: " + txtName.getText());

            closeModal();
        });
    }

    private void closeModal() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}