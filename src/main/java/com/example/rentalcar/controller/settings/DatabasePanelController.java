package com.example.rentalcar.controller.settings;

import com.example.rentalcar.utils.DBConnection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;

public class DatabasePanelController {
    @FXML private TextField txtDbHost, txtDbName;
    @FXML private Label lblConnectionStatus;

    public void initDatabase() {
        txtDbHost.setText("localhost:3306");
        txtDbName.setText("CarRentalDB");
    }

    @FXML void handleTestConnection() {
        lblConnectionStatus.setText("⏳  Đang kiểm tra...");
        lblConnectionStatus.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");

        new Thread(() -> {
            try {
                Thread.sleep(800);
                Connection conn = DBConnection.getInstance().getConnection();
                boolean ok = conn != null && !conn.isClosed();
                Platform.runLater(() -> {
                    if (ok) {
                        lblConnectionStatus.setText("✅  Kết nối thành công!");
                        lblConnectionStatus.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else {
                        lblConnectionStatus.setText("❌  Kết nối thất bại!");
                        lblConnectionStatus.setStyle("-fx-text-fill: #e11d48; -fx-font-weight: bold;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblConnectionStatus.setText("❌  Lỗi: " + e.getMessage());
                    lblConnectionStatus.setStyle("-fx-text-fill: #e11d48; -fx-font-weight: bold;");
                });
            }
        }).start();
    }

    @FXML void handleExportData() { new Alert(Alert.AlertType.INFORMATION, "📤  Tính năng đang phát triển!").showAndWait(); }
    @FXML void handleImportData() { new Alert(Alert.AlertType.WARNING, "📥  Tính năng đang phát triển!").showAndWait(); }
    @FXML void handleClearCache() { new Alert(Alert.AlertType.INFORMATION, "✅  Đã xóa cache hệ thống!").showAndWait(); }
}