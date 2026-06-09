package com.example.rentalcar.controller.settings;

import com.example.rentalcar.utils.DBConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.sql.Connection;

public class AboutPanelController {
    @FXML private Label lblJavaVersion, lblOS, lblDbStatus;

    public void initAbout() {
        lblJavaVersion.setText(System.getProperty("java.version"));
        lblOS.setText(System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        lblDbStatus.setText("Đang kiểm tra...");

        new Thread(() -> {
            try {
                Connection conn = DBConnection.getInstance().getConnection();
                boolean ok = conn != null && !conn.isClosed();
                Platform.runLater(() -> lblDbStatus.setText(ok ? "✅  Đã kết nối (MySQL 8.0)" : "❌  Mất kết nối"));
            } catch (Exception ignored) {
                Platform.runLater(() -> lblDbStatus.setText("❌  Lỗi kết nối"));
            }
        }).start();
    }
}