package com.example.rentalcar.controller.employee;

import com.example.rentalcar.bll.UserBLL;
import com.example.rentalcar.models.Users;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class AddEmployeeController {

    @FXML private TextField     txtFullName;
    @FXML private TextField     txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField     txtPhone;
    @FXML private TextField     txtEmail;
    @FXML private TextField     txtCccd;
    @FXML private TextField     txtAddress;
    @FXML private TextField     txtBirthDate;   // định dạng dd/MM/yyyy
    @FXML private ComboBox<String> cbRole;
    @FXML private ComboBox<String> cbGender;
    @FXML private Label         lblMsg;
    @FXML private Button        btnSave;

    private final UserBLL userBLL = new UserBLL();
    private Runnable onSaved;

    @FXML
    public void initialize() {
        cbRole.getItems().addAll("Staff", "Admin");
        cbRole.setValue("Staff");

        cbGender.getItems().addAll("Nam", "Nữ");
        cbGender.setValue("Nam");
    }

    public void setOnSaved(Runnable callback) {
        this.onSaved = callback;
    }

    // ── Lưu nhân viên mới ────────────────────────────────────
    @FXML
    void handleSave(ActionEvent event) {
        // --- Validate ---
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String confirm  = txtConfirmPassword.getText();
        String phone    = txtPhone.getText().trim();
        String email    = txtEmail.getText().trim();
        String cccd     = txtCccd.getText().trim();
        String address  = txtAddress.getText().trim();
        String birth    = txtBirthDate != null ? txtBirthDate.getText().trim() : "";

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || cccd.isEmpty()) {
            showMsg("❌  Vui lòng điền đầy đủ: Họ tên, Username, Mật khẩu, CCCD!", false);
            return;
        }
        if (password.length() < 8) {
            showMsg("❌  Mật khẩu phải có ít nhất 8 ký tự!", false);
            return;
        }
        if (!password.equals(confirm)) {
            showMsg("❌  Mật khẩu xác nhận không khớp!", false);
            return;
        }

        // --- Build Users object ---
        Users user = new Users();
        user.setFull_name(fullName);
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        user.setEmail(email.isEmpty() ? null : email);
        user.setCccd(cccd);
        user.setAddress(address);
        user.setGender("Nam".equals(cbGender.getValue()));
        user.setIs_active(true);

        // role_id: Admin=1, Staff=2
        user.setRole_id("Admin".equals(cbRole.getValue()) ? 1 : 2);

        // Ngày sinh (tùy chọn)
        if (!birth.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                user.setBirth_date(sdf.parse(birth));
            } catch (ParseException e) {
                showMsg("❌  Ngày sinh không hợp lệ (định dạng dd/MM/yyyy)!", false);
                return;
            }
        }

        // --- Gọi BLL ---
        try {
            boolean ok = userBLL.createUser(user);
            if (ok) {
                showMsg("✅  Thêm nhân viên thành công!", true);
                if (onSaved != null) onSaved.run();
                autoClose();
            } else {
                showMsg("❌  Lưu thất bại! Vui lòng thử lại.", false);
            }
        } catch (IllegalArgumentException ex) {
            showMsg("❌  " + ex.getMessage(), false);
        }
    }

    @FXML
    void handleClose(ActionEvent event) {
        closeStage();
    }

    // ── Helpers ───────────────────────────────────────────────
    private void showMsg(String text, boolean success) {
        if (lblMsg == null) return;
        lblMsg.setText(text);
        lblMsg.setStyle(success
                ? "-fx-text-fill: #16a34a; -fx-font-size: 13px; -fx-font-weight: bold;"
                : "-fx-text-fill: #e11d48; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblMsg.setVisible(true);
    }

    private void autoClose() {
        new Timeline(new KeyFrame(Duration.seconds(1.2), e -> closeStage())).play();
    }

    private void closeStage() {
        if (btnSave != null && btnSave.getScene() != null)
            ((Stage) btnSave.getScene().getWindow()).close();
    }
}