package com.example.rentalcar.controller.auth;

import com.example.rentalcar.bll.UserBLL;
import com.example.rentalcar.models.Users;
import com.example.rentalcar.utils.AppSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField     txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button        btnLogin;

    private final UserBLL userBLL = new UserBLL();

    @FXML
    void onLoginClick(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        try {
            // UserBLL.login() đã check: tồn tại, mật khẩu đúng, is_active=1
            Users user = userBLL.login(username, password);
            AppSession.setCurrentUser(user);
            navigateToMain(event);
        } catch (IllegalArgumentException ex) {
            showAlert(ex.getMessage());
        }
    }

    private void navigateToMain(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/views/MainView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("VehicleRent Pro – " + AppSession.getRoleDisplayName());
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi mở màn hình chính: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Đăng nhập thất bại");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}