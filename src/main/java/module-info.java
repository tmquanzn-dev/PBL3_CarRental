module PBL3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    // Mở package com.example.rentalcar.controller để JavaFX có thể ánh xạ các @FXML và hàm onLoginClick
    opens com.example.rentalcar.controller to javafx.fxml;

    // Mở package chứa models để JavaFX có thể truy cập properties của các model class
    opens com.example.rentalcar.models to javafx.base;

    // Mở package chứa file RentalApp để JavaFX có thể khởi chạy
    opens com.example.rentalcar to javafx.fxml;
    requires java.sql;
    requires jdk.jdi;
    exports com.example.rentalcar;
    opens com.example.rentalcar.controller.contract.booking to javafx.fxml;
    opens com.example.rentalcar.controller.contract to javafx.fxml;
    opens com.example.rentalcar.controller.auth to javafx.fxml;
    opens com.example.rentalcar.controller.dashboard to javafx.fxml;
}
