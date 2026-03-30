package com.example.rentalcar.controller.create_contract;

import com.example.rentalcar.dao.CustomerDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class Step1Controller {
    @FXML private TextField txtSearchCCCD, txtName, txtPhone, txtAddress;
    @FXML private ImageView imgFront, imgBack;

    @FXML
    void handleSearchCCCD() {
        String cccd = txtSearchCCCD.getText();
        System.out.println("Đang tìm khách hàng có CCCD: " + cccd);
        // Gọi DAO của Hà để lấy data khách hàng đổ vào txtName, txtPhone...
        CustomerDAO customerDAO = new CustomerDAO();
        customerDAO.findByCccd(cccd);
    }
}