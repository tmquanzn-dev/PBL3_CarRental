package com.example.rentalcar.controller.customer;

import com.example.rentalcar.bll.CustomerBLL;
import com.example.rentalcar.models.Customers;
import com.example.rentalcar.utils.ImageHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class AddCustomerController {

    @FXML private Button btnClose, btnCancel, btnSave;
    @FXML private TextField txtName, txtPhone, txtCccd, txtAddress, txtEmail;

    // ImageViews hiển thị preview
    @FXML private ImageView imgFront, imgBack;

    // Labels trạng thái upload (tuỳ chọn, thêm vào FXML nếu muốn)
    @FXML private Label lblFrontStatus, lblBackStatus;

    private final CustomerBLL customerBLL = new CustomerBLL();

    // Lưu đường dẫn ảnh để ghép thành JSON / String trước khi lưu DB
    private String pathFront = null;
    private String pathBack  = null;

    @FXML
    public void initialize() {
        btnClose.setOnAction(event -> closeModal());
        btnCancel.setOnAction(event -> closeModal());
        btnSave.setOnAction(event -> saveCustomerToDB());
    }

    // =========================================================
    //  PRE-FILL CCCD (gọi từ Step1Controller)
    // =========================================================
    public void setPreFillCccd(String cccd) {
        if (txtCccd != null) txtCccd.setText(cccd);
    }

    // =========================================================
    //  UPLOAD ẢNH CCCD MẶT TRƯỚC
    // =========================================================
    @FXML
    void handleUploadFront() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        String path = ImageHelper.chooseAndSave(stage, ImageHelper.Category.CCCD);
        if (path != null) {
            pathFront = path;
            ImageHelper.loadInto(imgFront, path);
            setStatus(lblFrontStatus, "✅ Đã tải ảnh mặt trước", true);
        }
    }

    // =========================================================
    //  UPLOAD ẢNH CCCD MẶT SAU
    // =========================================================
    @FXML
    void handleUploadBack() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        String path = ImageHelper.chooseAndSave(stage, ImageHelper.Category.CCCD);
        if (path != null) {
            pathBack = path;
            ImageHelper.loadInto(imgBack, path);
            setStatus(lblBackStatus, "✅ Đã tải ảnh mặt sau", true);
        }
    }

    // =========================================================
    //  LƯU KHÁCH HÀNG
    // =========================================================
    private void saveCustomerToDB() {
        try {
            String name    = txtName.getText().trim();
            String phone   = txtPhone.getText().trim();
            String cccd    = txtCccd.getText().trim();
            String address = txtAddress != null ? txtAddress.getText().trim() : "";
            String email   = txtEmail   != null ? txtEmail.getText().trim()   : "";

            if (name.isEmpty() || phone.isEmpty() || cccd.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo",
                        "Vui lòng nhập đầy đủ Tên, SĐT và CCCD!");
                return;
            }

            Customers c = new Customers();
            c.setFull_name(name);
            c.setPhone(phone);
            c.setCccd(cccd);
            c.setAddress(address);
            c.setEmail(email);
            c.setIs_blacklist(false);

            // Lưu đường dẫn ảnh CCCD (ghép 2 ảnh thành chuỗi phân cách dấu |)
            // Cột cccd_images trong DB có thể chứa "front|back"
            if (pathFront != null || pathBack != null) {
                String front = pathFront != null ? pathFront : "";
                String back  = pathBack  != null ? pathBack  : "";
                c.setCccd_images(front + "|" + back);
            }

            boolean isSuccess = customerBLL.addCustomer(c);
            if (isSuccess) {
                closeModal();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Thêm khách hàng thất bại!");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", e.getMessage());
        }
    }

    // =========================================================
    //  HELPERS
    // =========================================================
    private void setStatus(Label lbl, String msg, boolean ok) {
        if (lbl == null) return;
        lbl.setText(msg);
        lbl.setStyle(ok
                ? "-fx-text-fill: #16a34a; -fx-font-size: 12px; -fx-font-weight: bold;"
                : "-fx-text-fill: #e11d48; -fx-font-size: 12px;");
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void closeModal() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}