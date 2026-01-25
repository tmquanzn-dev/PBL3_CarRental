module PBL3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    // Mở package com.example.rentalcar.controller để JavaFX có thể ánh xạ các @FXML và hàm onLoginClick
    opens com.example.rentalcar.controller to javafx.fxml;

    // Mở package chứa file RentalApp để JavaFX có thể khởi chạy
    opens com.example.rentalcar to javafx.fxml;
    requires java.sql;
    exports com.example.rentalcar;
}
