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

public class EditEmployeeController {
    @FXML private Label lblTitle;
    @FXML private TextField txtFullName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCccd;
    @FXML private TextField txtAddress;
    @FXML private TextField txtBirthDate;   // dd/MM/yyyy
    @FXML private ComboBox<String> cbRole;
    @FXML private ComboBox<String> cbGender;
    @FXML private Label lblMsg;
    @FXML private Button btnSave;

    private final UserBLL userBLL = new UserBLL();
    private Users   currentUser;
    private Runnable onSaved;

    @FXML
    public void initialize() {
        cbRole.getItems().addAll("Staff", "Admin");
        cbGender.getItems().addAll("Nam", "Nữ");
    }

    public void setEmployee(Users user) {
        this.currentUser = user;
        if (user == null)
            return;

        if (lblTitle != null)
            lblTitle.setText("Sửa thông tin: " + user.getFull_name());

        if (txtFullName != null)
            txtFullName.setText(nvl(user.getFull_name()));
        if (txtPhone != null)
            txtPhone.setText(nvl(user.getPhone()));
        if (txtEmail != null)
            txtEmail.setText(nvl(user.getEmail()));
        if (txtCccd != null)
            txtCccd.setText(nvl(user.getCccd()));
        if (txtAddress != null)
            txtAddress.setText(nvl(user.getAddress()));

        if (txtBirthDate != null && user.getBirth_date() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            txtBirthDate.setText(sdf.format(user.getBirth_date()));
        }

        if (cbRole != null)
            cbRole.setValue(user.getRole_id() == 1 ? "Admin" : "Staff");
        if (cbGender != null)
            cbGender.setValue(user.isGender() ? "Nam" : "Nữ");
    }

    public void setOnSaved(Runnable callback) {
        this.onSaved = callback;
    }

    @FXML
    void handleSave(ActionEvent event) {
        String fullName = txtFullName != null ? txtFullName.getText().trim() : "";
        String phone = txtPhone != null ? txtPhone.getText().trim()    : "";
        String email = txtEmail != null ? txtEmail.getText().trim()    : "";
        String cccd = txtCccd != null ? txtCccd.getText().trim()     : "";
        String address = txtAddress  != null ? txtAddress.getText().trim()  : "";
        String birth = txtBirthDate != null ? txtBirthDate.getText().trim() : "";

        if (fullName.isEmpty()) {
            showMsg("❌  Họ tên không được để trống!", false);
            return;
        }

        currentUser.setFull_name(fullName);
        currentUser.setPhone(phone.isEmpty() ? null : phone);
        currentUser.setEmail(email.isEmpty() ? null : email);
        currentUser.setCccd(cccd.isEmpty() ? currentUser.getCccd() : cccd);
        currentUser.setAddress(address.isEmpty() ? null : address);
        currentUser.setGender("Nam".equals(cbGender != null ? cbGender.getValue() : "Nam"));
        currentUser.setRole_id("Admin".equals(cbRole != null ? cbRole.getValue() : "Staff") ? 1 : 2);

        if (!birth.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                currentUser.setBirth_date(sdf.parse(birth));
            } catch (ParseException e) {
                showMsg("❌  Ngày sinh không hợp lệ (dd/MM/yyyy)!", false);
                return;
            }
        }

        try {
            boolean ok = userBLL.updateUser(currentUser);
            if (ok) {
                showMsg("✅  Cập nhật thành công!", true);
                if (onSaved != null) onSaved.run();
                autoClose();
            } else {
                showMsg("❌  Cập nhật thất bại!", false);
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
    private String nvl(String s) {
        return s != null ? s : "";
    }

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