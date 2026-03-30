package com.example.rentalcar.controller;

import com.example.rentalcar.dao.UserDAO;
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

public class LoginController
{

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    void onLoginClick(ActionEvent event)
    {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText();

        // 1. Validate form cơ bản tránh bug ngớ ngẩn
        if (user.isEmpty() || pass.isEmpty())
        {
            showAlert("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        UserDAO userDAO = new UserDAO();

        // 2. Sử dụng hàm findByUsername theo chuẩn DAO chúng ta vừa thiết kế
        Users loginUser = userDAO.findByUsername(user);

        // 3. Kiểm tra mật khẩu (Thực tế sau này áp dụng thư viện BCrypt ở đây)
        // Lưu ý: Nếu DB của bạn em cột mật khẩu tên khác thì đổi loginUser.getPassword_hash() cho khớp nhé
        if (loginUser != null && pass.equals(loginUser.getPassword()))
        {

            // 4. Lưu Session chuẩn OOP
            AppSession.setCurrentUser(loginUser);

            try
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Node source = (Node) event.getSource();
                Stage stage = (Stage) source.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Hệ thống quản lý thuê xe - VehicleRent Pro");
                stage.centerOnScreen();
                stage.show();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                showAlert("Lỗi không thể mở màn hình chính: " + e.getMessage());
            }
        }
        else
        {
            showAlert("Sai tên đăng nhập hoặc mật khẩu!");
        }
    }

    private void showAlert(String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("LỖI ĐĂNG NHẬP");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}