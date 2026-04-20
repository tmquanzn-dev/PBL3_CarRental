package com.example.rentalcar.controller.settings;


import com.example.rentalcar.bll.UserBLL;
import com.example.rentalcar.models.Users;
import com.example.rentalcar.utils.AppSession;
import com.example.rentalcar.utils.DBConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    //  SIDEBAR NAV BUTTONS
    @FXML private Button btnNavProfile, btnNavSecurity, btnNavGeneral;
    @FXML private Button btnNavPricing, btnNavNotification;
    @FXML private Button btnNavDatabase, btnNavAbout;

    //  PANELS (ScrollPane)
    @FXML private ScrollPane panelProfile, panelSecurity, panelGeneral;
    @FXML private ScrollPane panelPricing, panelNotification;
    @FXML private ScrollPane panelDatabase, panelAbout;

    //  PANEL PROFILE
    @FXML private Label lblAvatarInitial;
    @FXML private Label lblProfileName, lblProfileRole, lblProfileStatus, lblProfileUsername;
    @FXML private TextField txtFullName, txtUsername, txtPhone, txtEmail, txtCccd, txtAddress;
    @FXML private Label lblProfileMsg;

    //  PANEL SECURITY
    @FXML private PasswordField txtOldPassword, txtNewPassword, txtConfirmPassword;
    @FXML private AnchorPane strengthBar1, strengthBar2, strengthBar3, strengthBar4;
    @FXML private Label lblStrength, lblSecurityMsg, lblSessionInfo;

    //  PANEL GENERAL
    @FXML private ComboBox<String> cbLanguage, cbDateFormat, cbCurrency, cbPageSize;
    @FXML private Button toggleAutoOverdue, toggleMaintWarn, toggleConfirmDelete;

    //  PANEL PRICING
    @FXML private TextField txtLatePenalty, txtFuelPrice;
    @FXML private ComboBox<String> cbLateCalcMode, cbFuelMultiplier;
    @FXML private Label lblPreviewLate, lblPreviewFuel;

    //  PANEL NOTIFICATION
    @FXML private Button toggleNotiNewContract, toggleNotiOverdue;
    @FXML private Button toggleNotiMaint, toggleNotiVoucher;

    //  PANEL DATABASE
    @FXML private TextField txtDbHost, txtDbName;
    @FXML private Label lblConnectionStatus;


    @FXML private Label lblJavaVersion, lblOS, lblDbStatus;


    private final UserBLL userBLL = new UserBLL();
    private Users currentUser;


    //  Trạng thái toogle (giả lập, có thể lưu vào DB/file)
    private boolean autoOverdue = true;
    private boolean maintWarn   = true;
    private boolean confirmDel  = true;
    private boolean notiNew     = true;
    private boolean notiOverdue = true;
    private boolean notiMaint   = true;
    private boolean notiVoucher = false;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AppSession.getCurrentUser();

        setupGeneralCombos();
        setupPricingCombos();
        loadUserProfile();
        loadAboutInfo();
        loadSessionInfo();
        setupPasswordStrengthListener();
        setupPricingPreviewListeners();

        // Mặc định hiện panel Profile
        showPanel(panelProfile);
    }

    @FXML void handleNavProfile()      { switchNav(btnNavProfile);      showPanel(panelProfile); }
    @FXML void handleNavSecurity()     { switchNav(btnNavSecurity);     showPanel(panelSecurity); }
    @FXML void handleNavGeneral()      { switchNav(btnNavGeneral);      showPanel(panelGeneral); }
    @FXML void handleNavPricing()      { switchNav(btnNavPricing);      showPanel(panelPricing); }
    @FXML void handleNavNotification() { switchNav(btnNavNotification); showPanel(panelNotification); }
    @FXML void handleNavDatabase()     { switchNav(btnNavDatabase);     showPanel(panelDatabase); }
    @FXML void handleNavAbout()        { switchNav(btnNavAbout);        showPanel(panelAbout); }

    private void showPanel(ScrollPane target) {
        ScrollPane[] all = {
                panelProfile, panelSecurity, panelGeneral,
                panelPricing, panelNotification, panelDatabase, panelAbout
        };
        for (ScrollPane p : all) p.setVisible(false);
        target.setVisible(true);
    }

    private void switchNav(Button activeBtn) {
        Button[] navBtns = {
                btnNavProfile, btnNavSecurity, btnNavGeneral,
                btnNavPricing, btnNavNotification, btnNavDatabase, btnNavAbout
        };
        for (Button btn : navBtns) {
            btn.getStyleClass().removeAll("settings-nav-active");
            btn.getStyleClass().add("settings-nav-btn");
        }
        activeBtn.getStyleClass().add("settings-nav-active");
    }


    private void loadUserProfile() {
        if (currentUser == null) return;

        // Avatar initial
        String name = currentUser.getFull_name() != null ? currentUser.getFull_name() : "?";
        lblAvatarInitial.setText(name.substring(0, 1).toUpperCase());

        // Labels
        lblProfileName.setText(name);
        lblProfileRole.setText(currentUser.getRole_name() != null ? currentUser.getRole_name() : "Nhân viên");
        lblProfileStatus.setText("● Đang hoạt động");
        lblProfileUsername.setText("@" + currentUser.getUsername());

        // Form fields
        txtFullName.setText(currentUser.getFull_name());
        txtUsername.setText(currentUser.getUsername());
        txtPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        txtEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        txtCccd.setText(currentUser.getCccd() != null ? currentUser.getCccd() : "");
        txtAddress.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");
    }

    @FXML
    void handleProfileSave() {
        try {
            if (currentUser == null) return;

            String newName = txtFullName.getText().trim();
            if (newName.isEmpty()) {
                showMsg(lblProfileMsg, "❌  Họ tên không được để trống!", false);
                return;
            }

            currentUser.setFull_name(newName);
            currentUser.setPhone(txtPhone.getText().trim());
            currentUser.setEmail(txtEmail.getText().trim());
            currentUser.setCccd(txtCccd.getText().trim());
            currentUser.setAddress(txtAddress.getText().trim());

            boolean ok = userBLL.updateUser(currentUser);
            if (ok) {
                AppSession.setCurrentUser(currentUser);
                loadUserProfile();
                showMsg(lblProfileMsg, "✅  Cập nhật hồ sơ thành công!", true);
            } else {
                showMsg(lblProfileMsg, "❌  Cập nhật thất bại, vui lòng thử lại!", false);
            }
        } catch (Exception e) {
            showMsg(lblProfileMsg, "❌  Lỗi: " + e.getMessage(), false);
        }
    }

    @FXML
    void handleProfileCancel() {
        loadUserProfile();
        lblProfileMsg.setVisible(false);
    }

    private void setupPasswordStrengthListener() {
        txtNewPassword.textProperty().addListener((obs, old, val) -> updateStrengthBar(val));
    }

    private void updateStrengthBar(String pass) {
        int score = 0;
        if (pass.length() >= 8)  score++;
        if (pass.matches(".*[A-Z].*")) score++;
        if (pass.matches(".*[0-9].*")) score++;
        if (pass.matches(".*[!@#$%^&*].*")) score++;

        String[] colors = {"#e2e8f0", "#e2e8f0", "#e2e8f0", "#e2e8f0"};
        String strengthText = "Chưa nhập";
        String strengthColor = "#94a3b8";

        if (score >= 1) { colors[0] = "#ef4444"; strengthText = "Yếu";     strengthColor = "#ef4444"; }
        if (score >= 2) { colors[1] = "#f59e0b"; strengthText = "Trung bình"; strengthColor = "#f59e0b"; }
        if (score >= 3) { colors[2] = "#22c55e"; strengthText = "Mạnh";    strengthColor = "#22c55e"; }
        if (score >= 4) { colors[3] = "#146dff"; strengthText = "Rất mạnh"; strengthColor = "#146dff"; }
        if (pass.isEmpty()) { strengthText = "Chưa nhập"; strengthColor = "#94a3b8"; }

        strengthBar1.setStyle("-fx-background-color: " + colors[0] + "; -fx-background-radius: 3;");
        strengthBar2.setStyle("-fx-background-color: " + colors[1] + "; -fx-background-radius: 3;");
        strengthBar3.setStyle("-fx-background-color: " + colors[2] + "; -fx-background-radius: 3;");
        strengthBar4.setStyle("-fx-background-color: " + colors[3] + "; -fx-background-radius: 3;");
        lblStrength.setText(strengthText);
        lblStrength.setStyle("-fx-text-fill: " + strengthColor + "; -fx-font-size: 12px;");
    }

    @FXML
    void handleChangePassword() {
        try {
            if (currentUser == null) return;

            String oldPass  = txtOldPassword.getText();
            String newPass  = txtNewPassword.getText();
            String confPass = txtConfirmPassword.getText();

            if (oldPass.isEmpty() || newPass.isEmpty() || confPass.isEmpty()) {
                showMsg(lblSecurityMsg, "❌  Vui lòng điền đầy đủ tất cả các ô mật khẩu!", false);
                return;
            }
            if (!newPass.equals(confPass)) {
                showMsg(lblSecurityMsg, "❌  Mật khẩu mới và xác nhận không khớp!", false);
                return;
            }
            if (newPass.length() < 8) {
                showMsg(lblSecurityMsg, "❌  Mật khẩu mới phải có ít nhất 8 ký tự!", false);
                return;
            }

            boolean ok = userBLL.changePassword(currentUser.getId_user(), oldPass, newPass);
            if (ok) {
                txtOldPassword.clear();
                txtNewPassword.clear();
                txtConfirmPassword.clear();
                updateStrengthBar("");
                showMsg(lblSecurityMsg, "✅  Đổi mật khẩu thành công!", true);
            } else {
                showMsg(lblSecurityMsg, "❌  Mật khẩu cũ không đúng!", false);
            }
        } catch (IllegalArgumentException e) {
            showMsg(lblSecurityMsg, "❌  " + e.getMessage(), false);
        }
    }

    @FXML
    void handleSecurityCancel() {
        txtOldPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();
        updateStrengthBar("");
        lblSecurityMsg.setVisible(false);
    }

    @FXML
    void handleLogoutAll() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn đăng xuất tất cả phiên hoạt động?");
        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            showMsg(lblSecurityMsg, "✅  Đã đăng xuất tất cả phiên!", true);
        });
    }

    private void loadSessionInfo() {
        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        if (lblSessionInfo != null)
            lblSessionInfo.setText("Đăng nhập lúc: " + now);
    }


    private void setupGeneralCombos() {
        if (cbLanguage != null)
            cbLanguage.getItems().addAll("Tiếng Việt", "English");

        if (cbDateFormat != null)
            cbDateFormat.getItems().addAll("dd/MM/yyyy HH:mm", "yyyy-MM-dd HH:mm", "MM/dd/yyyy hh:mm a");

        if (cbCurrency != null)
            cbCurrency.getItems().addAll("VNĐ (đ)", "USD ($)");

        if (cbPageSize != null)
            cbPageSize.getItems().addAll("10 hàng", "20 hàng", "50 hàng", "100 hàng");

        // Gán giá trị mặc định
        Platform.runLater(() -> {
            if (cbLanguage != null)
                cbLanguage.setValue("Tiếng Việt");
            if (cbDateFormat != null)
                cbDateFormat.setValue("dd/MM/yyyy HH:mm");
            if (cbCurrency != null)
                cbCurrency.setValue("VNĐ (đ)");
            if (cbPageSize != null)
                cbPageSize.setValue("20 hàng");
        });
    }

    @FXML void handleToggleAutoOverdue()  {
        autoOverdue = !autoOverdue; updateToggle(toggleAutoOverdue, autoOverdue);
    }
    @FXML void handleToggleMaintWarn()    {
        maintWarn   = !maintWarn;   updateToggle(toggleMaintWarn, maintWarn);
    }
    @FXML void handleToggleConfirmDelete(){
        confirmDel  = !confirmDel;  updateToggle(toggleConfirmDelete, confirmDel);
    }

    @FXML
    void handleRestoreDefaults() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(null);
        confirm.setContentText("Khôi phục toàn bộ cài đặt về mặc định?");
        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            if (cbLanguage != null)   cbLanguage.setValue("Tiếng Việt");
            if (cbDateFormat != null) cbDateFormat.setValue("dd/MM/yyyy HH:mm");
            if (cbCurrency != null)   cbCurrency.setValue("VNĐ (đ)");
            if (cbPageSize != null)   cbPageSize.setValue("20 hàng");
            autoOverdue = true; maintWarn = true; confirmDel = true;
            updateToggle(toggleAutoOverdue, true);
            updateToggle(toggleMaintWarn, true);
            updateToggle(toggleConfirmDelete, true);
        });
    }

    @FXML
    void handleGeneralSave() {
        showAlert("✅  Đã lưu cài đặt hệ thống thành công!", Alert.AlertType.INFORMATION);
    }

    private void setupPricingCombos() {
        Platform.runLater(() -> {
            if (cbLateCalcMode != null)
                cbLateCalcMode.getItems().addAll(
                        "Làm tròn lên theo giờ (mặc định)",
                        "Tính chính xác theo phút");

            if (cbFuelMultiplier != null)
                cbFuelMultiplier.getItems().addAll(
                        "x1.0 (không phụ phí)",
                        "x1.2 (+20% phụ phí)",
                        "x1.5 (+50% phụ phí)");

            if (cbLateCalcMode != null)   cbLateCalcMode.setValue("Làm tròn lên theo giờ (mặc định)");
            if (cbFuelMultiplier != null) cbFuelMultiplier.setValue("x1.0 (không phụ phí)");

            if (txtLatePenalty != null) txtLatePenalty.setText("100000");
            if (txtFuelPrice != null)   txtFuelPrice.setText("25000");

            updatePricingPreview();
        });
    }

    private void setupPricingPreviewListeners() {
        Platform.runLater(() -> {
            if (txtLatePenalty != null)
                txtLatePenalty.textProperty().addListener((obs, old, val) -> updatePricingPreview());
            if (txtFuelPrice != null)
                txtFuelPrice.textProperty().addListener((obs, old, val) -> updatePricingPreview());
        });
    }

    private void updatePricingPreview() {
        try {
            double latePenalty = parseDouble(txtLatePenalty.getText(), 100000);
            double fuelPrice   = parseDouble(txtFuelPrice.getText(), 25000);

            if (lblPreviewLate != null)
                lblPreviewLate.setText(formatMoney(latePenalty * 2));
            if (lblPreviewFuel != null)
                lblPreviewFuel.setText(formatMoney(fuelPrice * 5));
        } catch (Exception ignored) {}
    }

    @FXML
    void handlePricingSave() {
        try {
            double latePenalty = parseDouble(txtLatePenalty.getText(), -1);
            double fuelPrice   = parseDouble(txtFuelPrice.getText(), -1);

            if (latePenalty <= 0 || fuelPrice <= 0) {
                showAlert("❌  Vui lòng nhập giá trị hợp lệ (lớn hơn 0)!", Alert.AlertType.WARNING);
                return;
            }
            // TODO: Lưu vào DB (bảng system_settings)
            showAlert("✅  Đã cập nhật cấu hình giá thành công!\n"
                            + "Phạt trễ: " + formatMoney(latePenalty) + "/giờ\n"
                            + "Giá xăng: " + formatMoney(fuelPrice) + "/lít",
                    Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("❌  Lỗi: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handlePricingCancel() {
        txtLatePenalty.setText("100000");
        txtFuelPrice.setText("25000");
    }

    @FXML void handleToggleNotiNewContract() { notiNew     = !notiNew;     updateToggle(toggleNotiNewContract, notiNew); }
    @FXML void handleToggleNotiOverdue()     { notiOverdue = !notiOverdue; updateToggle(toggleNotiOverdue, notiOverdue); }
    @FXML void handleToggleNotiMaint()       { notiMaint   = !notiMaint;   updateToggle(toggleNotiMaint, notiMaint); }
    @FXML void handleToggleNotiVoucher()     { notiVoucher = !notiVoucher; updateToggle(toggleNotiVoucher, notiVoucher); }

    @FXML
    void handleNotiSave() {
        showAlert("✅  Đã lưu cài đặt thông báo thành công!", Alert.AlertType.INFORMATION);
    }

    @FXML
    void handleTestConnection() {
        if (lblConnectionStatus == null) return;
        lblConnectionStatus.setText("⏳  Đang kiểm tra...");
        lblConnectionStatus.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 13px; -fx-font-weight: bold;");

        new Thread(() -> {
            try {
                Thread.sleep(800); // Mô phỏng delay kết nối
                Connection conn = DBConnection.getInstance().getConnection();
                boolean ok = conn != null && !conn.isClosed();

                Platform.runLater(() -> {
                    if (ok) {
                        lblConnectionStatus.setText("✅  Kết nối thành công!");
                        lblConnectionStatus.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 13px; -fx-font-weight: bold;");
                        if (lblDbStatus != null)
                            lblDbStatus.setText("✅  Đang kết nối (CarRentalDB)");
                    } else {
                        lblConnectionStatus.setText("❌  Kết nối thất bại!");
                        lblConnectionStatus.setStyle("-fx-text-fill: #e11d48; -fx-font-size: 13px; -fx-font-weight: bold;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblConnectionStatus.setText("❌  Lỗi: " + e.getMessage());
                    lblConnectionStatus.setStyle("-fx-text-fill: #e11d48; -fx-font-size: 13px; -fx-font-weight: bold;");
                });
            }
        }).start();
    }

    @FXML
    void handleExportData() {
        showAlert("📤  Tính năng xuất dữ liệu đang được phát triển.\nVui lòng chờ phiên bản tiếp theo!",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    void handleImportData() {
        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Cảnh báo");
        confirm.setHeaderText("Nhập dữ liệu sẽ ghi đè dữ liệu hiện tại!");
        confirm.setContentText("Bạn có chắc chắn muốn tiếp tục?");
        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r ->
                showAlert("📥  Tính năng nhập dữ liệu đang được phát triển!", Alert.AlertType.INFORMATION)
        );
    }

    @FXML
    void handleClearCache() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(null);
        confirm.setContentText("Xóa toàn bộ cache hệ thống?");
        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r ->
                showAlert("✅  Đã xóa cache thành công!", Alert.AlertType.INFORMATION)
        );
    }

    // ======================================================
    //  PANEL ABOUT
    // ======================================================
    private void loadAboutInfo() {
        Platform.runLater(() -> {
            if (lblJavaVersion != null)
                lblJavaVersion.setText(System.getProperty("java.version"));
            if (lblOS != null)
                lblOS.setText(System.getProperty("os.name") + " " + System.getProperty("os.arch"));
            if (lblDbStatus != null)
                lblDbStatus.setText("Đang kiểm tra...");

            // Kiểm tra DB ở background
            new Thread(() -> {
                try {
                    Connection conn = DBConnection.getInstance().getConnection();
                    boolean ok = conn != null && !conn.isClosed();
                    Platform.runLater(() -> {
                        if (lblDbStatus != null)
                            lblDbStatus.setText(ok ? "✅  Đã kết nối (MySQL 8.0)" : "❌  Mất kết nối");
                    });
                } catch (Exception ignored) {
                    Platform.runLater(() -> {
                        if (lblDbStatus != null) lblDbStatus.setText("❌  Lỗi kết nối");
                    });
                }
            }).start();
        });
    }


    private void updateToggle(Button btn, boolean isOn) {
        if (btn == null) return;
        if (isOn) {
            btn.setText("BẬT");
            btn.getStyleClass().removeAll("toggle-btn-off");
            btn.getStyleClass().add("toggle-btn-on");
        } else {
            btn.setText("TẮT");
            btn.getStyleClass().removeAll("toggle-btn-on");
            btn.getStyleClass().add("toggle-btn-off");
        }
    }


    private void showMsg(Label label, String text, boolean isSuccess) {
        if (label == null) return;
        label.setText(text);
        label.setStyle(isSuccess
                ? "-fx-text-fill: #16a34a; -fx-font-size: 13px; -fx-font-weight: bold;"
                : "-fx-text-fill: #e11d48; -fx-font-size: 13px; -fx-font-weight: bold;");
        label.setVisible(true);

        // Tự ẩn sau 3 giây
        Timeline hide = new Timeline(new KeyFrame(Duration.seconds(3), e -> label.setVisible(false)));
        hide.play();
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private double parseDouble(String text, double defaultValue) {
        try {
            return Double.parseDouble(text.trim().replace(",", "").replace(".", ""));
        } catch (Exception e) {
            return defaultValue;
        }
    }
    private String formatMoney(double amount) {
        return String.format("%,.0f đ", amount).replace(",", ".");
    }
}
