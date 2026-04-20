package com.example.rentalcar.controller.contract.booking;

import com.example.rentalcar.bll.CustomerBLL;
import com.example.rentalcar.controller.customer.AddCustomerController;
import com.example.rentalcar.models.Customers;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Step1Controller {
    @FXML private TextField txtSearchCCCD, txtName, txtPhone, txtAddress;
    @FXML private ImageView imgFront, imgBack;

    private final CustomerBLL customerBLL = new CustomerBLL();

    @FXML
    void handleSearchCCCD() {
        String cccd = txtSearchCCCD.getText().trim();
        if (cccd.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số CCCD để tìm kiếm!");
            return;
        }

        try {
            Customers cus = customerBLL.findByCccd(cccd);

            if (cus != null) {
                // Đã có khách -> Điền tự động vào Form Step 1
                txtName.setText(cus.getFull_name());
                txtPhone.setText(cus.getPhone());
                txtAddress.setText(cus.getAddress() != null ? cus.getAddress() : "");
            } else {
                // Không có khách -> Gợi ý bật form Thêm Mới
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Khách hàng mới");
                confirm.setHeaderText("Không tìm thấy thông tin!");
                confirm.setContentText("CCCD " + cccd + " chưa có trong hệ thống.\nBạn có muốn thêm khách hàng này không?");

                confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                    openAddCustomerModal(cccd);
                });
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
        }
    }

    private void openAddCustomerModal(String cccd) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/customer/AddCustomerModal.fxml"));
            Parent root = loader.load();

            // Ép CCCD vừa nhập sang form Thêm Khách để nhân viên khỏi gõ lại
            AddCustomerController controller = loader.getController();
            controller.setPreFillCccd(cccd);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Thêm khách hàng mới");
            stage.initModality(Modality.APPLICATION_MODAL); // Bắt buộc phải tắt Modal mới thao tác tiếp được

            // Lệnh showAndWait sẽ dừng code ở đây cho đến khi tắt Modal
            stage.showAndWait();

            // Sau khi tắt form Thêm Khách, tự động dò lại DB và Fill vào Form Step 1
            Customers newCus = customerBLL.findByCccd(cccd);
            if (newCus != null) {
                txtName.setText(newCus.getFull_name());
                txtPhone.setText(newCus.getPhone());
                txtAddress.setText(newCus.getAddress() != null ? newCus.getAddress() : "");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể mở form thêm khách hàng!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}