package com.example.rentalcar.controller.settings;

import com.example.rentalcar.bll.UserBLL;
import com.example.rentalcar.utils.AppSession;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SecurityPanelController {
    @FXML private PasswordField txtOldPassword, txtNewPassword, txtConfirmPassword;
    @FXML private AnchorPane strengthBar1, strengthBar2, strengthBar3, strengthBar4;
    @FXML private Label lblStrength, lblSecurityMsg, lblSessionInfo;

    private final UserBLL userBLL = new UserBLL();

    public void initSecurity() {
        txtNewPassword.textProperty().addListener((obs, old, val) -> updateStrengthBar(val));
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        lblSessionInfo.setText("Đăng nhập lúc: " + now);
    }

    private void updateStrengthBar(String pass) {
        int score = 0;
        if (pass.length() >= 8) score++; if (pass.matches(".*[A-Z].*")) score++; if (pass.matches(".*[0-9].*"))
            score++;
        if (pass.matches(".*[!@#$%^&*].*"))
            score++;

        String[] colors = {"#e2e8f0","#e2e8f0","#e2e8f0","#e2e8f0"};
        String text = "Chưa nhập", color = "#94a3b8";

        if (score >= 1) {
            colors[0] = "#ef4444";
            text = "Yếu";
            color = "#ef4444";
        }

        if (score >= 2) {
            colors[1] = "#f59e0b";
            text = "Trung bình";
            color = "#f59e0b";
        }

        if (score >= 3) {
            colors[2] = "#22c55e";
            text = "Mạnh";
            color = "#22c55e";
        }

        if (score >= 4) {
            colors[3] = "#146dff";
            text = "Rất mạnh";
            color = "#146dff";
        }

        if (pass.isEmpty()) {
            text = "Chưa nhập";
            color = "#94a3b8"; }

        String style = "-fx-background-radius: 3; -fx-background-color: ";
        strengthBar1.setStyle(style + colors[0] + ";");
        strengthBar2.setStyle(style + colors[1] + ";");
        strengthBar3.setStyle(style + colors[2] + ";");
        strengthBar4.setStyle(style + colors[3] + ";");
        lblStrength.setText(text);
        lblStrength.setStyle("-fx-text-fill: " + color + ";");
    }

    @FXML void handleChangePassword() {
        String oldPass = txtOldPassword.getText();
        String newPass = txtNewPassword.getText();
        String confPass = txtConfirmPassword.getText();

        if (oldPass.isEmpty() || newPass.isEmpty() || confPass.isEmpty()) {
            showMsg("❌ Điền đầy đủ ô mật khẩu!", false);
            return;
        }

        if (!newPass.equals(confPass)) {
            showMsg("❌ Mật khẩu mới không khớp!", false);
            return;
        }

        if (newPass.length() < 8) {
            showMsg("❌ Mật khẩu phải có ít nhất 8 ký tự!", false);
            return;
        }

        if (userBLL.changePassword(AppSession.getCurrentUser().getId_user(), oldPass, newPass)) {
            txtOldPassword.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();
            updateStrengthBar("");
            showMsg("✅  Đổi mật khẩu thành công!", true);
        } else {
            showMsg("❌  Mật khẩu cũ không đúng!", false);
        }
    }

    @FXML void handleSecurityCancel() {
        txtOldPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();
        updateStrengthBar("");
    }

    @FXML void handleLogoutAll() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn đăng xuất tất cả phiên?");
        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> showMsg("✅  Đã đăng xuất tất cả phiên!", true));
    }

    private void showMsg(String text, boolean success) {
        lblSecurityMsg.setText(text);
        lblSecurityMsg.setStyle(success ? "-fx-text-fill:#16a34a;-fx-font-weight:bold;" : "-fx-text-fill:#e11d48;-fx-font-weight:bold;");
        lblSecurityMsg.setVisible(true);
        new Timeline(new KeyFrame(Duration.seconds(3), e -> lblSecurityMsg.setVisible(false))).play();
    }
}