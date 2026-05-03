module PBL3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires jdk.jdi;

    // ✅ iText 7 modules – cần thiết để tạo PDF
    requires kernel;
    requires layout;
    requires io;
    requires commons;

    // Mở package cho JavaFX
    opens com.example.rentalcar.controller to javafx.fxml;
    opens com.example.rentalcar.models to javafx.base;
    opens com.example.rentalcar to javafx.fxml;
    opens com.example.rentalcar.controller.contract.booking to javafx.fxml;
    opens com.example.rentalcar.controller.contract to javafx.fxml;
    opens com.example.rentalcar.controller.auth to javafx.fxml;
    opens com.example.rentalcar.controller.vehicle to javafx.fxml;
    opens com.example.rentalcar.controller.dashboard to javafx.fxml;
    opens com.example.rentalcar.controller.customer to javafx.fxml;
    opens com.example.rentalcar.controller.employee to javafx.fxml;
    opens com.example.rentalcar.controller.rule to javafx.fxml;
    opens com.example.rentalcar.controller.report to javafx.fxml;
    opens com.example.rentalcar.controller.settings to javafx.fxml;
    opens com.example.rentalcar.controller.voucher to javafx.fxml;
    opens com.example.rentalcar.controller.partprice to javafx.fxml;
    opens com.example.rentalcar.bll to javafx.fxml;

    exports com.example.rentalcar;
}