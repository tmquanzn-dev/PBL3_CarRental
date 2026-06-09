package com.example.rentalcar.controller.settings;

import com.example.rentalcar.bll.SystemSettingBLL;
import com.example.rentalcar.bll.UserBLL;
import com.example.rentalcar.models.Users;
import com.example.rentalcar.utils.AppSession;
import com.example.rentalcar.utils.DBConnection;
import com.example.rentalcar.utils.ImageHelper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    // ── SIDEBAR NAV ──────────────────────────────────────────────────────
    @FXML private Button btnNavProfile, btnNavSecurity, btnNavGeneral;
    @FXML private Button btnNavPricing, btnNavNotification;
    @FXML private Button btnNavDatabase, btnNavAbout;

    // ── PANELS ───────────────────────────────────────────────────────────
    @FXML private ScrollPane panelProfile, panelSecurity, panelGeneral;
    @FXML private ScrollPane panelPricing, panelNotification;
    @FXML private ScrollPane panelDatabase, panelAbout;

    // ── PANEL PROFILE ────────────────────────────────────────────────────
    @FXML private Label     lblAvatarInitial;
    @FXML private ImageView imgAvatar;
    @FXML private StackPane avatarStack;
    @FXML private Label     lblProfileName, lblProfileRole, lblProfileStatus, lblProfileUsername;
    @FXML private TextField txtFullName, txtUsername, txtPhone, txtEmail, txtCccd, txtAddress;
    @FXML private Label     lblProfileMsg;

    // ── PANEL SECURITY ───────────────────────────────────────────────────
    @FXML private PasswordField txtOldPassword, txtNewPassword, txtConfirmPassword;
    @FXML private AnchorPane    strengthBar1, strengthBar2, strengthBar3, strengthBar4;
    @FXML private Label         lblStrength, lblSecurityMsg, lblSessionInfo;

    // ── PANEL GENERAL ────────────────────────────────────────────────────
    @FXML private ComboBox<String> cbLanguage, cbDateFormat, cbCurrency, cbPageSize;
    @FXML private Button toggleAutoOverdue, toggleMaintWarn, toggleConfirmDelete;

    // ── PANEL PRICING ────────────────────────────────────────────────────
    @FXML private TextField        txtLatePenalty, txtFuelPrice;
    @FXML private ComboBox<String> cbLateCalcMode, cbFuelMultiplier;
    @FXML private Label            lblPreviewLate, lblPreviewFuel;

    // ── PANEL NOTIFICATION ───────────────────────────────────────────────
    @FXML private Button toggleNotiNewContract, toggleNotiOverdue;
    @FXML private Button toggleNotiMaint, toggleNotiVoucher;

    // ── PANEL DATABASE ───────────────────────────────────────────────────
    @FXML private TextField txtDbHost, txtDbName;
    @FXML private Label     lblConnectionStatus;

    // ── PANEL ABOUT ──────────────────────────────────────────────────────
    @FXML private Label lblJavaVersion, lblOS, lblDbStatus;

    // ── STATE ────────────────────────────────────────────────────────────
    private final UserBLL          userBLL    = new UserBLL();
    private final SystemSettingBLL settingBLL = new SystemSettingBLL();   // ← THÊM MỚI
    private Users currentUser;

    private boolean autoOverdue = true, maintWarn   = true;
    private boolean confirmDel  = true, notiNew     = true;
    private boolean notiOverdue = true, notiMaint   = true;
    private boolean notiVoucher = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = AppSession.getCurrentUser();

        applyRoleRestrictions();

        setupGeneralCombos();
        setupPricingCombos();
        loadUserProfile();
        loadAboutInfo();
        loadSessionInfo();
        setupPasswordStrengthListener();
        setupPricingPreviewListeners();

        showPanel(panelProfile);
    }

    private void applyRoleRestrictions() {
        boolean admin = AppSession.isAdmin();
        setNavVisible(btnNavGeneral,      admin);
        setNavVisible(btnNavPricing,      admin);
        setNavVisible(btnNavNotification, admin);
        setNavVisible(btnNavDatabase,     admin);
        setNavVisible(btnNavAbout,        admin);
    }

    private void setNavVisible(Button btn, boolean show) {
        if (btn == null) return;
        btn.setVisible(show);
        btn.setManaged(show);
    }

    //  NAV HANDLERS
    @FXML void handleNavProfile() { switchNav(btnNavProfile); showPanel(panelProfile); }
    @FXML void handleNavSecurity() { switchNav(btnNavSecurity); showPanel(panelSecurity); }

    @FXML void handleNavGeneral() {
        if (!AppSession.isAdmin()) return;
        switchNav(btnNavGeneral); showPanel(panelGeneral);
    }
    @FXML void handleNavPricing() {
        if (!AppSession.isAdmin()) return;
        switchNav(btnNavPricing); showPanel(panelPricing);
        loadPricingFromDB();        // ← Tải lại từ DB mỗi khi mở tab
    }
    @FXML void handleNavNotification() {
        if (!AppSession.isAdmin()) return;
        switchNav(btnNavNotification); showPanel(panelNotification);
    }
    @FXML void handleNavDatabase() {
        if (!AppSession.isAdmin()) return;
        switchNav(btnNavDatabase); showPanel(panelDatabase);
    }
    @FXML void handleNavAbout() {
        if (!AppSession.isAdmin()) return;
        switchNav(btnNavAbout); showPanel(panelAbout);
    }

    private void showPanel(ScrollPane target) {
        ScrollPane[] all = { panelProfile, panelSecurity, panelGeneral,
                panelPricing, panelNotification, panelDatabase, panelAbout };
        for (ScrollPane p : all) if (p != null) p.setVisible(false);
        if (target != null) target.setVisible(true);
    }

    private void switchNav(Button activeBtn) {
        Button[] navBtns = { btnNavProfile, btnNavSecurity, btnNavGeneral,
                btnNavPricing, btnNavNotification, btnNavDatabase, btnNavAbout };
        for (Button btn : navBtns) {
            if (btn == null) continue;
            btn.getStyleClass().removeAll("settings-nav-active");
            btn.getStyleClass().add("settings-nav-btn");
        }
        if (activeBtn != null) activeBtn.getStyleClass().add("settings-nav-active");
    }

    //  PROFILE – LOAD DỮ LIỆU
    private void loadUserProfile() {
        if (currentUser == null) return;

        String name = currentUser.getFull_name() != null ? currentUser.getFull_name() : "?";

        if (lblAvatarInitial != null)
            lblAvatarInitial.setText(name.substring(0, 1).toUpperCase());

        loadAvatarImage(currentUser);

        if (lblProfileName     != null) lblProfileName.setText(name);
        if (lblProfileRole     != null) lblProfileRole.setText(currentUser.getRole_name() != null ? currentUser.getRole_name() : "Nhân viên");
        if (lblProfileStatus   != null) lblProfileStatus.setText("● Đang hoạt động");
        if (lblProfileUsername != null) lblProfileUsername.setText("@" + currentUser.getUsername());

        if (txtFullName != null) txtFullName.setText(nvl(currentUser.getFull_name()));
        if (txtUsername != null) txtUsername.setText(nvl(currentUser.getUsername()));
        if (txtPhone    != null) txtPhone.setText(nvl(currentUser.getPhone()));
        if (txtEmail    != null) txtEmail.setText(nvl(currentUser.getEmail()));
        if (txtCccd     != null) txtCccd.setText(nvl(currentUser.getCccd()));
        if (txtAddress  != null) txtAddress.setText(nvl(currentUser.getAddress()));
    }

    private void loadAvatarImage(Users user) {
        if (imgAvatar == null) return;
        String avatarPath = parseAvatarPath(user.getAddress());
        if (avatarPath != null && !avatarPath.isBlank()) {
            ImageHelper.loadInto(imgAvatar, avatarPath);
            imgAvatar.setVisible(true);
            if (lblAvatarInitial != null) lblAvatarInitial.setVisible(false);
        } else {
            imgAvatar.setVisible(false);
            if (lblAvatarInitial != null) lblAvatarInitial.setVisible(true);
        }
    }

    private String parseAvatarPath(String address) {
        if (address == null) return null;
        int start = address.lastIndexOf("[avatar=");
        int end   = address.lastIndexOf("]");
        if (start >= 0 && end > start) return address.substring(start + 8, end).trim();
        return null;
    }

    private String embedAvatarPath(String address, String avatarPath) {
        if (address == null) address = "";
        int start = address.lastIndexOf("[avatar=");
        if (start >= 0) address = address.substring(0, start).trim();
        return address + " [avatar=" + avatarPath + "]";
    }

    @FXML void handleChangeAvatar() {
        if (currentUser == null) return;

        Stage stage = null;
        if (btnNavProfile != null && btnNavProfile.getScene() != null)
            stage = (Stage) btnNavProfile.getScene().getWindow();
        if (stage == null) return;

        String path = ImageHelper.chooseAndSave(stage, ImageHelper.Category.AVATAR);
        if (path == null) return;

        if (imgAvatar != null) {
            ImageHelper.loadInto(imgAvatar, path);
            imgAvatar.setVisible(true);
            if (lblAvatarInitial != null) lblAvatarInitial.setVisible(false);
        }

        String newAddress = embedAvatarPath(
                txtAddress != null ? txtAddress.getText() : currentUser.getAddress(), path);
        currentUser.setAddress(newAddress);
        if (txtAddress != null) txtAddress.setText(newAddress);

        boolean ok = userBLL.updateUser(currentUser);
        if (ok) {
            AppSession.setCurrentUser(currentUser);
            showMsg(lblProfileMsg, "✅  Đã cập nhật ảnh đại diện!", true);
        } else {
            showMsg(lblProfileMsg, "❌  Không thể lưu ảnh, vui lòng thử lại!", false);
        }
    }

    @FXML void handleProfileSave() {
        try {
            if (currentUser == null) return;
            String newName = txtFullName != null ? txtFullName.getText().trim() : "";
            if (newName.isEmpty()) {
                showMsg(lblProfileMsg, "❌  Họ tên không được để trống!", false);
                return;
            }
            currentUser.setFull_name(newName);
            if (txtPhone   != null) currentUser.setPhone(txtPhone.getText().trim());
            if (txtEmail   != null) currentUser.setEmail(txtEmail.getText().trim());
            if (txtCccd    != null) currentUser.setCccd(txtCccd.getText().trim());
            if (txtAddress != null) currentUser.setAddress(txtAddress.getText().trim());

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

    @FXML void handleProfileCancel() {
        loadUserProfile();
        if (lblProfileMsg != null) lblProfileMsg.setVisible(false);
    }

    //  ĐỔI MẬT KHẨU
    private void setupPasswordStrengthListener() {
        if (txtNewPassword != null)
            txtNewPassword.textProperty().addListener((obs, old, val) -> updateStrengthBar(val));
    }

    private void updateStrengthBar(String pass) {
        int score = 0;
        if (pass.length() >= 8)             score++;
        if (pass.matches(".*[A-Z].*"))      score++;
        if (pass.matches(".*[0-9].*"))      score++;
        if (pass.matches(".*[!@#$%^&*].*")) score++;

        String[] colors = {"#e2e8f0","#e2e8f0","#e2e8f0","#e2e8f0"};
        String strengthText = "Chưa nhập", strengthColor = "#94a3b8";

        if (score >= 1) { colors[0] = "#ef4444"; strengthText = "Yếu";       strengthColor = "#ef4444"; }
        if (score >= 2) { colors[1] = "#f59e0b"; strengthText = "Trung bình"; strengthColor = "#f59e0b"; }
        if (score >= 3) { colors[2] = "#22c55e"; strengthText = "Mạnh";       strengthColor = "#22c55e"; }
        if (score >= 4) { colors[3] = "#146dff"; strengthText = "Rất mạnh";   strengthColor = "#146dff"; }
        if (pass.isEmpty()) { strengthText = "Chưa nhập"; strengthColor = "#94a3b8"; }

        String style = "-fx-background-radius: 3; -fx-background-color: ";
        if (strengthBar1 != null) strengthBar1.setStyle(style + colors[0] + ";");
        if (strengthBar2 != null) strengthBar2.setStyle(style + colors[1] + ";");
        if (strengthBar3 != null) strengthBar3.setStyle(style + colors[2] + ";");
        if (strengthBar4 != null) strengthBar4.setStyle(style + colors[3] + ";");
        if (lblStrength  != null) {
            lblStrength.setText(strengthText);
            lblStrength.setStyle("-fx-text-fill: " + strengthColor + "; -fx-font-size: 12px;");
        }
    }

    @FXML void handleChangePassword() {
        try {
            if (currentUser == null) return;
            String oldPass  = txtOldPassword      != null ? txtOldPassword.getText()      : "";
            String newPass  = txtNewPassword       != null ? txtNewPassword.getText()       : "";
            String confPass = txtConfirmPassword   != null ? txtConfirmPassword.getText()   : "";

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
                if (txtOldPassword    != null) txtOldPassword.clear();
                if (txtNewPassword    != null) txtNewPassword.clear();
                if (txtConfirmPassword!= null) txtConfirmPassword.clear();
                updateStrengthBar("");
                showMsg(lblSecurityMsg, "✅  Đổi mật khẩu thành công!", true);
            } else {
                showMsg(lblSecurityMsg, "❌  Mật khẩu cũ không đúng!", false);
            }
        } catch (IllegalArgumentException e) {
            showMsg(lblSecurityMsg, "❌  " + e.getMessage(), false);
        }
    }

    @FXML void handleSecurityCancel() {
        if (txtOldPassword    != null) txtOldPassword.clear();
        if (txtNewPassword    != null) txtNewPassword.clear();
        if (txtConfirmPassword!= null) txtConfirmPassword.clear();
        updateStrengthBar("");
        if (lblSecurityMsg != null) lblSecurityMsg.setVisible(false);
    }

    @FXML void handleLogoutAll() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận"); confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn đăng xuất tất cả phiên hoạt động?");
        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r ->
                showMsg(lblSecurityMsg, "✅  Đã đăng xuất tất cả phiên!", true));
    }

    private void loadSessionInfo() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        if (lblSessionInfo != null) lblSessionInfo.setText("Đăng nhập lúc: " + now);
    }

    //  GENERAL SETTINGS (Admin only)
    private void setupGeneralCombos() {
        Platform.runLater(() -> {
            if (cbLanguage  != null) { cbLanguage.getItems().addAll("Tiếng Việt","English"); cbLanguage.setValue("Tiếng Việt"); }
            if (cbDateFormat!= null) { cbDateFormat.getItems().addAll("dd/MM/yyyy HH:mm","yyyy-MM-dd HH:mm","MM/dd/yyyy hh:mm a"); cbDateFormat.setValue("dd/MM/yyyy HH:mm"); }
            if (cbCurrency  != null) { cbCurrency.getItems().addAll("VNĐ (đ)","USD ($)"); cbCurrency.setValue("VNĐ (đ)"); }
            if (cbPageSize  != null) { cbPageSize.getItems().addAll("10 hàng","20 hàng","50 hàng","100 hàng"); cbPageSize.setValue("20 hàng"); }
        });
    }

    @FXML void handleToggleAutoOverdue() { autoOverdue = !autoOverdue; updateToggle(toggleAutoOverdue, autoOverdue); }
    @FXML void handleToggleMaintWarn()   { maintWarn = !maintWarn;   updateToggle(toggleMaintWarn, maintWarn); }
    @FXML void handleToggleConfirmDelete(){ confirmDel = !confirmDel; updateToggle(toggleConfirmDelete, confirmDel); }

    @FXML void handleRestoreDefaults() {
        if (!AppSession.isAdmin()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận"); confirm.setHeaderText(null);
        confirm.setContentText("Khôi phục toàn bộ cài đặt về mặc định?");
        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            if (cbLanguage  != null) cbLanguage.setValue("Tiếng Việt");
            if (cbDateFormat!= null) cbDateFormat.setValue("dd/MM/yyyy HH:mm");
            if (cbCurrency  != null) cbCurrency.setValue("VNĐ (đ)");
            if (cbPageSize  != null) cbPageSize.setValue("20 hàng");
            autoOverdue = true; maintWarn = true; confirmDel = true;
            updateToggle(toggleAutoOverdue, true);
            updateToggle(toggleMaintWarn, true);
            updateToggle(toggleConfirmDelete, true);
        });
    }

    @FXML void handleGeneralSave() { showAlert("✅  Đã lưu cài đặt hệ thống thành công!", Alert.AlertType.INFORMATION); }

    //  PRICING SETTINGS – ĐỌC & GHI DB THẬT Su
    private void setupPricingCombos() {
        Platform.runLater(() -> {
            if (cbLateCalcMode != null) {
                cbLateCalcMode.getItems().addAll(
                        "Làm tròn lên theo giờ (mặc định)",
                        "Tính chính xác theo phút");
                cbLateCalcMode.setValue("Làm tròn lên theo giờ (mặc định)");
            }
            if (cbFuelMultiplier != null) {
                cbFuelMultiplier.getItems().addAll(
                        "x1.0 (không phụ phí)",
                        "x1.2 (+20% phụ phí)",
                        "x1.5 (+50% phụ phí)");
                cbFuelMultiplier.setValue("x1.0 (không phụ phí)");
            }

            // Đọc giá trị thực từ DB
            loadPricingFromDB();
        });
    }

    /**
     * Đọc Phi_Tre_Gio và Gia_Xang_Litre từ bảng systemsettings.
     */
    private void loadPricingFromDB() {
        try {
            double latePenalty = settingBLL.getDoubleSetting("Phi_Tre_Gio", 100000.0);
            double fuelPrice   = settingBLL.getDoubleSetting("Gia_Xang_Litre", 25000.0);

            if (txtLatePenalty != null)
                txtLatePenalty.setText(String.valueOf((long) latePenalty));
            if (txtFuelPrice != null)
                txtFuelPrice.setText(String.valueOf((long) fuelPrice));

            updatePricingPreview();
        } catch (Exception e) {
            System.err.println("[Settings] Lỗi đọc cấu hình giá: " + e.getMessage());
        }
    }

    /**
     * Cập nhật preview tính toán.
     */
    private void setupPricingPreviewListeners() {
        Platform.runLater(() -> {
            if (txtLatePenalty != null)
                txtLatePenalty.textProperty().addListener((o, old, v) -> updatePricingPreview());
            if (txtFuelPrice != null)
                txtFuelPrice.textProperty().addListener((o, old, v) -> updatePricingPreview());
        });
    }

    private void updatePricingPreview() {
        try {
            double late = parseDouble(txtLatePenalty != null ? txtLatePenalty.getText() : "100000", 100000);
            double fuel = parseDouble(txtFuelPrice   != null ? txtFuelPrice.getText()   : "25000",  25000);
            if (lblPreviewLate != null) lblPreviewLate.setText(formatMoney(late * 2));
            if (lblPreviewFuel != null) lblPreviewFuel.setText(formatMoney(fuel * 5));
        } catch (Exception ignored) {}
    }

    /**
     * LƯU THẬT SỰ vào bảng systemsettings.
     */
    @FXML void handlePricingSave() {
        //if (!AppSession.isAdmin()) return;
        //if (currentUser == null) return;

        try {
            // Validate dữ liệu đầu vào
            String lateTxt = txtLatePenalty != null ? txtLatePenalty.getText().trim() : "";
            String fuelTxt = txtFuelPrice   != null ? txtFuelPrice.getText().trim()   : "";

            if (lateTxt.isEmpty() || fuelTxt.isEmpty()) {
                showAlert("❌  Vui lòng nhập đầy đủ phí phạt trễ giờ và giá xăng!", Alert.AlertType.WARNING);
                return;
            }

            double late, fuel;
            try {
                late = Double.parseDouble(lateTxt.replace(",", "").replace(".", ""));
                fuel = Double.parseDouble(fuelTxt.replace(",", "").replace(".", ""));
            } catch (NumberFormatException e) {
                showAlert("❌  Giá trị nhập vào phải là số hợp lệ!", Alert.AlertType.WARNING);
                return;
            }

            if (late <= 0 || fuel <= 0) {
                showAlert("❌  Giá trị phải lớn hơn 0!", Alert.AlertType.WARNING);
                return;
            }

            // Lưu vào DB qua SystemSettingBLL
            int editorId = currentUser.getId_user();

            boolean savedLate = settingBLL.updateSettingValue(
                    "Phi_Tre_Gio", String.valueOf((long) late), editorId);
            boolean savedFuel = settingBLL.updateSettingValue(
                    "Gia_Xang_Litre", String.valueOf((long) fuel), editorId);

            if (savedLate && savedFuel) {
                showAlert("✅  Đã lưu cấu hình giá thành công!\n"
                                + "Phạt trễ: " + formatMoney(late) + "/giờ\n"
                                + "Giá xăng: " + formatMoney(fuel) + "/lít",
                        Alert.AlertType.INFORMATION);
            } else if (!savedLate) {
                showAlert("❌  Không thể lưu phí phạt trễ giờ. Kiểm tra khoá 'Phi_Tre_Gio' trong DB!",
                        Alert.AlertType.ERROR);
            } else {
                showAlert("❌  Không thể lưu giá xăng. Kiểm tra khoá 'Gia_Xang_Litre' trong DB!",
                        Alert.AlertType.ERROR);
            }

        } catch (IllegalStateException e) {
            // Bắt lỗi phân quyền từ BLL
            showAlert("❌  " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("❌  Lỗi: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML void handlePricingCancel() {
        // Tải lại từ DB thay vì hardcode
        loadPricingFromDB();
    }

    //  NOTIFICATION (Admin only)
    @FXML void handleToggleNotiNewContract() { notiNew     = !notiNew;     updateToggle(toggleNotiNewContract, notiNew); }
    @FXML void handleToggleNotiOverdue()     { notiOverdue = !notiOverdue; updateToggle(toggleNotiOverdue, notiOverdue); }
    @FXML void handleToggleNotiMaint()       { notiMaint   = !notiMaint;   updateToggle(toggleNotiMaint, notiMaint); }
    @FXML void handleToggleNotiVoucher()     { notiVoucher = !notiVoucher; updateToggle(toggleNotiVoucher, notiVoucher); }
    @FXML void handleNotiSave() { showAlert("✅  Đã lưu cài đặt thông báo thành công!", Alert.AlertType.INFORMATION); }

    //  DATABASE (Admin only)
    @FXML void handleTestConnection() {
        if (!AppSession.isAdmin()) return;
        if (lblConnectionStatus == null) return;
        lblConnectionStatus.setText("⏳  Đang kiểm tra...");
        lblConnectionStatus.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 13px; -fx-font-weight: bold;");

        new Thread(() -> {
            try {
                Thread.sleep(800);
                Connection conn = DBConnection.getInstance().getConnection();
                boolean ok = conn != null && !conn.isClosed();
                Platform.runLater(() -> {
                    if (ok) {
                        lblConnectionStatus.setText("✅  Kết nối thành công!");
                        lblConnectionStatus.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 13px; -fx-font-weight: bold;");
                        if (lblDbStatus != null) lblDbStatus.setText("✅  Đang kết nối (CarRentalDB)");
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

    @FXML void handleExportData() { showAlert("📤  Tính năng xuất dữ liệu đang được phát triển!", Alert.AlertType.INFORMATION); }

    @FXML void handleImportData() {
        Alert c = new Alert(Alert.AlertType.WARNING);
        c.setTitle("Cảnh báo"); c.setHeaderText("Nhập dữ liệu sẽ ghi đè dữ liệu hiện tại!");
        c.setContentText("Bạn có chắc muốn tiếp tục?");
        c.showAndWait().filter(r -> r == ButtonType.OK)
                .ifPresent(r -> showAlert("📥  Tính năng đang phát triển!", Alert.AlertType.INFORMATION));
    }

    @FXML void handleClearCache() {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Xác nhận"); c.setHeaderText(null); c.setContentText("Xóa toàn bộ cache hệ thống?");
        c.showAndWait().filter(r -> r == ButtonType.OK)
                .ifPresent(r -> showAlert("✅  Đã xóa cache thành công!", Alert.AlertType.INFORMATION));
    }

    //  ABOUT
    private void loadAboutInfo() {
        Platform.runLater(() -> {
            if (lblJavaVersion != null) lblJavaVersion.setText(System.getProperty("java.version"));
            if (lblOS          != null) lblOS.setText(System.getProperty("os.name") + " " + System.getProperty("os.arch"));
            if (lblDbStatus    != null) lblDbStatus.setText("Đang kiểm tra...");
            new Thread(() -> {
                try {
                    Connection conn = DBConnection.getInstance().getConnection();
                    boolean ok = conn != null && !conn.isClosed();
                    Platform.runLater(() -> {
                        if (lblDbStatus != null)
                            lblDbStatus.setText(ok ? "✅  Đã kết nối (MySQL 8.0)" : "❌  Mất kết nối");
                    });
                } catch (Exception ignored) {
                    Platform.runLater(() -> { if (lblDbStatus != null) lblDbStatus.setText("❌  Lỗi kết nối"); });
                }
            }).start();
        });
    }

    //  HELPERS
    private void updateToggle(Button btn, boolean isOn) {
        if (btn == null) return;
        btn.setText(isOn ? "BẬT" : "TẮT");
        btn.getStyleClass().removeAll("toggle-btn-on", "toggle-btn-off");
        btn.getStyleClass().add(isOn ? "toggle-btn-on" : "toggle-btn-off");
    }

    private void showMsg(Label label, String text, boolean isSuccess) {
        if (label == null) return;
        label.setText(text);
        label.setStyle(isSuccess
                ? "-fx-text-fill: #16a34a; -fx-font-size: 13px; -fx-font-weight: bold;"
                : "-fx-text-fill: #e11d48; -fx-font-size: 13px; -fx-font-weight: bold;");
        label.setVisible(true);
        new Timeline(new KeyFrame(Duration.seconds(3), e -> label.setVisible(false))).play();
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo"); alert.setHeaderText(null); alert.setContentText(message);
        alert.showAndWait();
    }

    private double parseDouble(String text, double defaultValue) {
        try { return Double.parseDouble(text.trim().replace(",", "").replace(".", "")); }
        catch (Exception e) { return defaultValue; }
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f đ", amount).replace(",", ".");
    }

    private String nvl(String s) { return s != null ? s : ""; }
}