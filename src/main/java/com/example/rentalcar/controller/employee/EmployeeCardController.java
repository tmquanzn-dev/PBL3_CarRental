package com.example.rentalcar.controller.employee;

import com.example.rentalcar.bll.ReportBLL;
import com.example.rentalcar.bll.UserBLL;
import com.example.rentalcar.models.Users;
import com.example.rentalcar.utils.AppSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

public class EmployeeCardController {

    @FXML private Label  lblFullName, lblRoleName, lblUsername, lblOrderCount, lblStatus, lblInitial;
    @FXML private Circle circleAvatar;
    @FXML private Button btnEdit;
    @FXML private Button btnLock;
    @FXML private Button btnResetPass;
    @FXML private HBox   btnBox;

    private Users    currentUser;
    private Runnable onRefresh;
    private final UserBLL userBLL = new UserBLL();
    private final ReportBLL reportBLL = new ReportBLL();

    public void setData(Users user) {
        if (user == null) return;
        this.currentUser = user;

        if (lblFullName   != null) lblFullName.setText(user.getFull_name());
        if (lblRoleName   != null) lblRoleName.setText(
                user.getRole_name() != null ? user.getRole_name() : "Nhân viên");
        if (lblUsername   != null) lblUsername.setText("@" + user.getUsername());

        // FIX: lấy số đơn tháng này thay vì hardcode "0 đơn"
        if (lblOrderCount != null) {
            int count = reportBLL.getStaffPerformanceCount(user.getId_user());
            String month = java.time.LocalDate.now().getMonthValue() + "/" +
                    java.time.LocalDate.now().getYear();
            lblOrderCount.setText(count + " đơn T" + month);
        }

        if (lblInitial != null && user.getFull_name() != null && !user.getFull_name().isEmpty())
            lblInitial.setText(user.getFull_name().substring(0, 1).toUpperCase());

        refreshStatusBadge(user.isIs_active());
        refreshLockButton(user.isIs_active());

        boolean isAdmin = AppSession.isAdmin();
        if (btnBox != null) {
            btnBox.setVisible(isAdmin);
            btnBox.setManaged(isAdmin);
        }

        if (circleAvatar != null) {
            circleAvatar.setFill(user.getRole_id() == 1
                    ? javafx.scene.paint.Color.web("#ef4444")
                    : javafx.scene.paint.Color.web("#4f46e5"));
        }
    }

    public void setOnRefresh(Runnable callback) { this.onRefresh = callback; }

    private void refreshStatusBadge(boolean isActive) {
        if (lblStatus == null) return;
        lblStatus.getStyleClass().removeAll("status-active", "status-locked");
        if (isActive) {
            lblStatus.setText("Hoạt động");
            lblStatus.getStyleClass().add("status-active");
        } else {
            lblStatus.setText("Đã khóa");
            lblStatus.getStyleClass().add("status-locked");
        }
    }

    private void refreshLockButton(boolean isActive) {
        if (btnLock == null) return;
        if (isActive) {
            btnLock.setText("🔒  Khóa");
            btnLock.setStyle("-fx-background-color:#ef4444;-fx-text-fill:white;"
                    + "-fx-background-radius:8;-fx-cursor:hand;-fx-font-weight:bold;");
        } else {
            btnLock.setText("🔓  Mở");
            btnLock.setStyle("-fx-background-color:#22c55e;-fx-text-fill:white;"
                    + "-fx-background-radius:8;-fx-cursor:hand;-fx-font-weight:bold;");
        }
    }

    @FXML
    void handleEdit() {
        if (currentUser == null || !AppSession.isAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/employee/EditEmployeeModal.fxml"));
            Parent root = loader.load();
            EditEmployeeController ctrl = loader.getController();
            ctrl.setEmployee(currentUser);
            ctrl.setOnSaved(() -> { if (onRefresh != null) onRefresh.run(); });

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Sửa: " + currentUser.getFull_name());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (Exception e) {
            showError("Lỗi mở form sửa: " + e.getMessage());
        }
    }

    @FXML
    void handleLock() {
        if (currentUser == null || !AppSession.isAdmin()) return;
        if (AppSession.getCurrentUser() != null
                && AppSession.getCurrentUser().getId_user() == currentUser.getId_user()) {
            showError("Bạn không thể khóa tài khoản của chính mình!"); return;
        }

        boolean isActive = currentUser.isIs_active();
        String action = isActive ? "khóa" : "mở khóa";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận " + action);
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn " + action
                + " tài khoản \"" + currentUser.getFull_name() + "\"?");

        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                boolean ok = isActive
                        ? userBLL.lockUser(currentUser.getId_user())
                        : userBLL.unlockUser(currentUser.getId_user());
                if (ok) {
                    currentUser.setIs_active(!isActive);
                    refreshStatusBadge(!isActive);
                    refreshLockButton(!isActive);
                    showInfo((isActive ? "Đã khóa" : "Đã mở khóa")
                            + " tài khoản " + currentUser.getFull_name() + "!");
                    if (onRefresh != null) onRefresh.run();
                } else {
                    showError("Thao tác thất bại!");
                }
            } catch (IllegalArgumentException ex) { showError(ex.getMessage()); }
        });
    }

    @FXML
    void handleResetPassword() {
        if (currentUser == null || !AppSession.isAdmin()) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset mật khẩu");
        dialog.setHeaderText("Nhân viên: " + currentUser.getFull_name()
                + "  (@" + currentUser.getUsername() + ")");
        dialog.setContentText("Mật khẩu mới (tối thiểu 8 ký tự):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPass -> {
            if (newPass.trim().length() < 8) {
                showError("Mật khẩu mới phải có ít nhất 8 ký tự!"); return;
            }
            try {
                boolean ok = userBLL.resetPassword(currentUser.getId_user(), newPass.trim());
                if (ok) showInfo("Đã reset mật khẩu cho " + currentUser.getFull_name() + "!");
                else showError("Reset mật khẩu thất bại!");
            } catch (IllegalArgumentException ex) { showError(ex.getMessage()); }
        });
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Lỗi"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Thông báo"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}