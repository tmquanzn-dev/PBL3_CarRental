package com.example.rentalcar.controller;

import com.example.rentalcar.dao.UserDAO;
import com.example.rentalcar.models.Users;
import com.example.rentalcar.utils.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    void onLoginClick(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        UserDAO userDAO = new UserDAO();
        Users loginUser = userDAO.login(user, pass);

        if (loginUser != null) {
            AppSession.currentUser = loginUser;
            System.out.println("Chào mừng " + loginUser.getFull_name());
        }
        else {
            showAlert("Sai tên đăng nhập hoặc mật khẩu!");
        }
    }
    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("LỖI");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}