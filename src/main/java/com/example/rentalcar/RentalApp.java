package com.example.rentalcar;

import javafx.application.Application;
import javafx.stage.Stage;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

public class RentalApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Đường dẫn này trỏ vào thư mục views chứa file FXML
        FXMLLoader fxmlLoader = new FXMLLoader(RentalApp.class.getResource("/views/LoginView.fxml"));
        // Tạo Scene với kích thước phù hợp (ví dụ 800x500)
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Hệ Thống Quản Lý Thuê Xe - PBL3");
        stage.setScene(scene);
        stage.setResizable(false); // Không cho phép co giãn để giữ giao diện đẹp
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}