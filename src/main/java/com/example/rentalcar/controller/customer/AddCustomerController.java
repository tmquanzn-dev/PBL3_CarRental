package com.example.rentalcar.controller.customer;

import com.example.rentalcar.bll.CustomerBLL;
import com.example.rentalcar.models.Customers;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class AddCustomerController {

    @FXML private Button btnClose, btnCancel, btnSave;
    @FXML private TextField txtName, txtPhone, txtCccd, txtAddress, txtEmail;
    @FXML private ImageView imgFront, imgBack;

    private final CustomerBLL customerBLL = new CustomerBLL();

    @FXML
    public void initialize() {
        btnClose.setOnAction(event -> closeModal());
        btnCancel.setOnAction(event -> closeModal());

        // Bắt sự kiện bấm nút Lưu
        btnSave.setOnAction(event -> saveCustomerToDB());
    }

    // Hàm này để Step1Controller truyền CCCD sang tự động
    public void setPreFillCccd(String cccd) {
        if (txtCccd != null) {
            txtCccd.setText(cccd);
        }
    }

    private void saveCustomerToDB() {
        try {
            // Lấy dữ liệu
            String name = txtName.getText().trim();
            String phone = txtPhone.getText().trim();
            String cccd = txtCccd.getText().trim();
            String address = txtAddress.getText() != null ? txtAddress.getText().trim() : "";
            String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";

            if (name.isEmpty() || phone.isEmpty() || cccd.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ Tên, SĐT và CCCD!");
                return;
            }

            // Gán dữ liệu vào Object (Model của Quân)
            Customers c = new Customers();
            c.setFull_name(name);
            c.setPhone(phone);
            c.setCccd(cccd);
            c.setAddress(address);
            c.setEmail(email);
            c.setIs_blacklist(false); // Khách mới thì chắc chắn chưa có tiền án tiền sự

            // Gọi BLL lưu xuống MySQL
            boolean isSuccess = customerBLL.addCustomer(c);

            if (isSuccess) {
                // Tắt form ngay lập tức sau khi lưu thành công
                closeModal();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Thêm khách hàng thất bại!");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", e.getMessage());
        }
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