package com.example.rentalcar.controller.settings;

import com.example.rentalcar.bll.UserBLL;
import com.example.rentalcar.models.Users;
import com.example.rentalcar.utils.AppSession;
import com.example.rentalcar.utils.ImageHelper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ProfilePanelController {
    @FXML private Label lblAvatarInitial, lblProfileName, lblProfileRole, lblProfileStatus, lblProfileUsername, lblProfileMsg;
    @FXML private ImageView imgAvatar;
    @FXML private StackPane avatarStack;
    @FXML private TextField txtFullName, txtUsername, txtPhone, txtEmail, txtCccd, txtAddress;

    private final UserBLL userBLL = new UserBLL();
    private Users currentUser;

    public void initData() {
        this.currentUser = AppSession.getCurrentUser();
        loadUserProfile();
    }

    private void loadUserProfile() {
        if (currentUser == null) return;
        String name = currentUser.getFull_name() != null ? currentUser.getFull_name() : "?";
        lblAvatarInitial.setText(name.substring(0, 1).toUpperCase());
        loadAvatarImage(currentUser);

        lblProfileName.setText(name);
        lblProfileRole.setText(currentUser.getRole_name() != null ? currentUser.getRole_name() : "Nhân viên");
        lblProfileUsername.setText("@" + currentUser.getUsername());

        txtFullName.setText(currentUser.getFull_name());
        txtUsername.setText(currentUser.getUsername());
        txtPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        txtEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        txtCccd.setText(currentUser.getCccd() != null ? currentUser.getCccd() : "");
        txtAddress.setText(parseCleanAddress(currentUser.getAddress()));
    }

    private void loadAvatarImage(Users user) {
        String avatarPath = parseAvatarPath(user.getAddress());
        if (avatarPath != null && !avatarPath.isBlank()) {
            ImageHelper.loadInto(imgAvatar, avatarPath);
            imgAvatar.setVisible(true);
            lblAvatarInitial.setVisible(false);
        } else {
            imgAvatar.setVisible(false);
            lblAvatarInitial.setVisible(true);
        }
    }

    @FXML void handleChangeAvatar() {
        Stage stage = (Stage) txtFullName.getScene().getWindow();
        String path = ImageHelper.chooseAndSave(stage, ImageHelper.Category.AVATAR);
        if (path == null)
            return;

        ImageHelper.loadInto(imgAvatar, path);
        imgAvatar.setVisible(true);
        lblAvatarInitial.setVisible(false);

        String newAddress = embedAvatarPath(txtAddress.getText(), path);
        currentUser.setAddress(newAddress);

        if (userBLL.updateUser(currentUser)) {
            AppSession.setCurrentUser(currentUser);
            showMsg("✅  Đã cập nhật ảnh đại diện!", true);
        }
    }

    @FXML void handleProfileSave() {
        String newName = txtFullName.getText().trim();
        if (newName.isEmpty()) {
            showMsg("❌  Họ tên không được để trống!", false);
            return;
        }
        currentUser.setFull_name(newName);
        currentUser.setPhone(txtPhone.getText().trim());
        currentUser.setEmail(txtEmail.getText().trim());
        currentUser.setCccd(txtCccd.getText().trim());
        currentUser.setAddress(embedAvatarPath(txtAddress.getText().trim(), parseAvatarPath(currentUser.getAddress())));

        if (userBLL.updateUser(currentUser)) {
            AppSession.setCurrentUser(currentUser);
            loadUserProfile();
            showMsg("✅  Cập nhật hồ sơ thành công!", true);
        }
    }

    @FXML void handleProfileCancel() {
        loadUserProfile();
    }

    //Tìm đường dẫn ảnh
    private String parseAvatarPath(String address) {
        if (address == null)
            return null;
        int start = address.lastIndexOf("[avatar=");
        int end = address.lastIndexOf("]");
        return (start >= 0 && end > start) ? address.substring(start + 8, end).trim() : null;
    }

    //Trả đường dẫn ảnh
    private String parseCleanAddress(String address) {
        if (address == null) return "";
        int start = address.lastIndexOf("[avatar=");
        return start >= 0 ? address.substring(0, start).trim() : address;
    }

    private String embedAvatarPath(String address, String avatarPath) {
        if (address == null) address = "";
        int start = address.lastIndexOf("[avatar=");
        if (start >= 0) address = address.substring(0, start).trim();
        return avatarPath != null ? address + " [avatar=" + avatarPath + "]" : address;
    }

    private void showMsg(String text, boolean success) {
        lblProfileMsg.setText(text);
        lblProfileMsg.setStyle(success ? "-fx-text-fill: #16a34a; -fx-font-weight: bold;" : "-fx-text-fill: #e11d48; -fx-font-weight: bold;");
        lblProfileMsg.setVisible(true);
        new Timeline(new KeyFrame(Duration.seconds(3), e -> lblProfileMsg.setVisible(false))).play();
    }
}