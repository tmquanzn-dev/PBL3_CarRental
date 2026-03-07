package com.example.rentalcar.controller;

import com.example.rentalcar.dao.UserDAO;
import com.example.rentalcar.models.Users;
import com.example.rentalcar.utils.AppSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;

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
            try {
                FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/MainView.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Node source = (Node) event.getSource();
                Stage stage = (Stage) source.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Hệ thống quản lý thuê xe - VehicleRent Pro");
                stage.centerOnScreen();
                stage.show();
            }
            catch (IOException e) {
                e.printStackTrace();
                showAlert("Lỗi không thể mở màn hình chính: " + e.getMessage());
            }
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