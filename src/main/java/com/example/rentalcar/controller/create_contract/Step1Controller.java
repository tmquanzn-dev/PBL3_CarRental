package com.example.rentalcar.controller.create_contract;

import com.example.rentalcar.dao.CustomerDAO;
import com.example.rentalcar.models.Customers;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Step1Controller {
    @FXML
    private TextField txtSearchCCCD, txtName, txtPhone, txtAddress;
    @FXML
    private ImageView imgFront, imgBack;
    @FXML
    private Button btnUploadFront, btnUploadBack;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private Customers currentCustomer;
    private File frontImageFile;
    private File backImageFile;


    @FXML
    public void initialize() {
        txtSearchCCCD.setOnAction(e -> handleSearchCCCD());
        setFormEditable(false);
    }


    @FXML
    void handleSearchCCCD() {
        String cccd = txtSearchCCCD.getText().trim();

        if (cccd.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số CCCD trước khi tìm kiếm.");
            return;
        }
        if (!cccd.matches("\\d{12}")) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mã số CCCD phải là số có 12 chữ số.");
            return;
        }


        Customers found = customerDAO.findByCccd(cccd);

        if (found != null) {
            // เช็ค Blacklist ก่อน
            if (found.isIs_blacklist()) {
                showAlert(Alert.AlertType.ERROR,
                        "khách hàng Blacklist",
                        "khách hàng: " + found.getFull_name() + "\n" +
                                "lý do: " + found.getBlacklist_reason() + "\n\n" +
                                "Hợp đồng thuê nhà không thể được ký kết.");
                clearForm();
                setFormEditable(false);
                return;
            }
            currentCustomer = found;
            txtName.setText(nvl(found.getFull_name()));
            txtPhone.setText(nvl(found.getPhone()));
            txtAddress.setText(nvl(found.getAddress()));
            setFormEditable(true);

            if (found.getTrust_score() < 50) {
                showAlert(Alert.AlertType.WARNING,
                        "Điểm độ tin cậy thấp.",
                        "Trust Score: " + found.getTrust_score() + "/100\nVui lòng cân nhắc kỹ trước khi phê duyệt.");
            }

        } else {
            // ลูกค้าใหม่
            currentCustomer = null;
            clearForm();
            setFormEditable(true);
            showAlert(Alert.AlertType.INFORMATION, "Khách hàng mới",
                    "Không tìm thấy CCCD: " + cccd + "\nVui lòng điền thông tin khách hàng mới.");
        }
    }

    public Customers getValidatedCustomer() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();
        String cccd = txtSearchCCCD.getText().trim();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thông tin chưa đầy đủ.", "Nhập Họ và tên của bạn.");
            txtName.requestFocus();
            return null;
        }
        if (!phone.matches("0\\d{9}")) {
            showAlert(Alert.AlertType.WARNING, "Thông tin chưa đầy đủ.", "Số điện thoại");
            txtPhone.requestFocus();
            return null;
        }
        if (address.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thông tin chưa đầy đủ.", "Nhập địa chỉ");
            txtAddress.requestFocus();
            return null;
        }

        if (currentCustomer != null) {
            currentCustomer.setFull_name(name);
            currentCustomer.setPhone(phone);
            currentCustomer.setAddress(address);
            return currentCustomer;
        } else {
            Customers newCustomer = new Customers();
            newCustomer.setCccd(cccd);
            newCustomer.setFull_name(name);
            newCustomer.setPhone(phone);
            newCustomer.setAddress(address);
            newCustomer.setBlacklist_reason("");
            boolean saved = customerDAO.insert(newCustomer);
            if (!saved) {
                showAlert(Alert.AlertType.ERROR, "error", "Lưu không thành công. Vui lòng kiểm tra kết nối của bạn.");
                return null;
            }
            currentCustomer = newCustomer;
            return newCustomer;
        }
    }

    public File getFrontImageFile() {
        return frontImageFile;
    }

    public File getBackImageFile() {
        return backImageFile;
    }

    public Customers getCurrentCustomer() {
        return currentCustomer;
    }

    private void setFormEditable(boolean editable) {
        txtName.setDisable(!editable);
        txtPhone.setDisable(!editable);
        txtAddress.setDisable(!editable);
        if (btnUploadFront != null) btnUploadFront.setDisable(!editable);
        if (btnUploadBack != null) btnUploadBack.setDisable(!editable);
    }

    private void clearForm() {
        txtName.clear();
        txtPhone.clear();
        txtAddress.clear();
        imgFront.setImage(null);
        imgBack.setImage(null);
        frontImageFile = null;
        backImageFile = null;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String nvl(String v) {
        return v != null ? v : "";
    }
}