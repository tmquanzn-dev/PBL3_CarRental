package com.example.rentalcar.controller.customer;

import com.example.rentalcar.bll.CustomerBLL;
import com.example.rentalcar.models.Customers;
import com.example.rentalcar.utils.ImageHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class CustomerEditController {

    @FXML private Label     lblCustomerId;
    @FXML private TextField txtName, txtPhone, txtCccd, txtAddress, txtEmail;
    @FXML private ImageView imgFront, imgBack;
    @FXML private Label     lblFrontStatus, lblBackStatus;

    private final CustomerBLL customerBLL = new CustomerBLL();
    private Customers customers;

    // Lưu đường dẫn ảnh mới (nếu người dùng upload lại)
    private String newPathFront = null;
    private String newPathBack  = null;

    //  NHẬN DỮ LIỆU KHÁCH HÀNG + LOAD ẢNH CŨ
    public void setCustomerData(Customers customer) {
        if (customer == null) return;
        this.customers = customer;

        lblCustomerId.setText("ID: " + customer.getId_customer());
        txtName.setText(customer.getFull_name());
        txtPhone.setText(customer.getPhone() != null ? customer.getPhone() : "");
        txtCccd.setText(customer.getCccd());
        txtEmail.setText(customer.getEmail() != null ? customer.getEmail() : "");
        txtAddress.setText(customer.getAddress() != null ? customer.getAddress() : "");

        // Load ảnh CCCD cũ nếu có
        loadExistingImages(customer.getCccd_images());
    }

    /**
     * Phân tích chuỗi "front|back" lưu trong DB và load vào ImageView.
     */
    private void loadExistingImages(String cccdImages) {
        if (cccdImages == null || cccdImages.isBlank()) return;

        String[] parts = cccdImages.split("\\|", -1);
        String front = parts.length > 0 ? parts[0].trim() : "";
        String back  = parts.length > 1 ? parts[1].trim() : "";

        if (!front.isEmpty()) {
            ImageHelper.loadInto(imgFront, front);
            setStatus(lblFrontStatus, "✅ Ảnh mặt trước đã có", true);
        }
        if (!back.isEmpty()) {
            ImageHelper.loadInto(imgBack, back);
            setStatus(lblBackStatus, "✅ Ảnh mặt sau đã có", true);
        }
    }

    //  UPLOAD ẢNH MỚI
    @FXML
    void handleUploadFront(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        String path = ImageHelper.chooseAndSave(stage, ImageHelper.Category.CCCD);
        if (path != null) {
            newPathFront = path;
            ImageHelper.loadInto(imgFront, path);
            setStatus(lblFrontStatus, "✅ Đã thay ảnh mặt trước", true);
        }
    }

    @FXML
    void handleUploadBack(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        String path = ImageHelper.chooseAndSave(stage, ImageHelper.Category.CCCD);
        if (path != null) {
            newPathBack = path;
            ImageHelper.loadInto(imgBack, path);
            setStatus(lblBackStatus, "✅ Đã thay ảnh mặt sau", true);
        }
    }

    //  LƯU THAY ĐỔI
    @FXML
    void handleSave(ActionEvent event) {
        customers.setFull_name(txtName.getText().trim());
        customers.setPhone(txtPhone.getText().trim());
        customers.setCccd(txtCccd.getText().trim());
        customers.setEmail(txtEmail.getText().trim());
        customers.setAddress(txtAddress.getText().trim());

        // Cập nhật ảnh CCCD: chỉ thay phần nào người dùng upload lại
        String existingImages = customers.getCccd_images() != null
                ? customers.getCccd_images() : "|";
        String[] parts = existingImages.split("\\|", -1);
        String front = parts.length > 0 ? parts[0].trim() : "";
        String back  = parts.length > 1 ? parts[1].trim() : "";

        if (newPathFront != null) front = newPathFront;
        if (newPathBack  != null) back  = newPathBack;

        customers.setCccd_images(front + "|" + back);

        try {
            boolean success = customerBLL.updateCustomer(customers);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Đã cập nhật thông tin khách hàng!");
                closeStage(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật thất bại!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
        }
    }

    @FXML
    void handleClose(ActionEvent event) {
        closeStage(event);
    }

    //  HELPERS
    private void setStatus(Label lbl, String msg, boolean ok) {
        if (lbl == null) return;
        lbl.setText(msg);
        lbl.setStyle(ok
                ? "-fx-text-fill: #16a34a; -fx-font-size: 12px; -fx-font-weight: bold;"
                : "-fx-text-fill: #e11d48; -fx-font-size: 12px;");
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void closeStage(ActionEvent event) {
        Node node   = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
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