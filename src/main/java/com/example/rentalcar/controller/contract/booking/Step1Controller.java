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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Step1Controller {

    @FXML private TextField txtSearchCCCD;
    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtAddress;
    @FXML private HBox boxBlacklistWarning;
    @FXML private Label lblBlacklistReason;

    private ContractDraft draft;
    private Customers foundCustomer;

    private final CustomerBLL customerBLL = new CustomerBLL();

    public void setDraft(ContractDraft draft) {
        this.draft = draft;
        if (draft.getSelectedCustomer() != null) {
            fillForm(draft.getSelectedCustomer());
            foundCustomer = draft.getSelectedCustomer();
        }
    }

    @FXML
    void handleSearchCCCD() {
        String cccd = txtSearchCCCD.getText().trim();
        if (cccd.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo",
                    "Vui lòng nhập số CCCD để tìm kiếm!");
            return;
        }
        hideBlacklistWarning();

        try {
            Customers cus = customerBLL.findByCccd(cccd);

            if (cus != null) {
                if (cus.isIs_blacklist()) {
                    showBlacklistWarning(cus);
                    clearForm();
                    foundCustomer = null;
                    return;
                }
                fillForm(cus);
                foundCustomer = cus;

            } else {
                // Không tìm thấy → gợi ý thêm mới
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Khách hàng mới");
                confirm.setHeaderText("Không tìm thấy thông tin!");
                confirm.setContentText(
                        "CCCD " + cccd + " chưa có trong hệ thống.\n"
                                + "Bạn có muốn thêm khách hàng này không?");

                confirm.showAndWait()
                        .filter(r -> r == ButtonType.OK)
                        .ifPresent(r -> openAddCustomerModal(cccd));
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
        }
    }

    private void openAddCustomerModal(String cccd) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/customer/AddCustomerModal.fxml"));
            Parent root = loader.load();

            AddCustomerController ctrl = loader.getController();
            ctrl.setPreFillCccd(cccd);

            // nhận lại CCCD thực tế đã lưu qua callback
            ctrl.setOnSaved(savedCccd -> {
                try {
                    Customers newCus = customerBLL.findByCccd(savedCccd);
                    if (newCus != null && !newCus.isIs_blacklist()) {
                        fillForm(newCus);
                        foundCustomer = newCus;
                        // Cập nhật ô tìm kiếm cho khớp
                        if (txtSearchCCCD != null) {
                            txtSearchCCCD.setText(savedCccd);
                        }
                        hideBlacklistWarning();
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi",
                            "Không thể tải thông tin khách: " + e.getMessage());
                }
            });

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Thêm khách hàng mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống",
                    "Không thể mở form thêm khách hàng!");
        }
    }


    public boolean validateAndSave() {
        if (foundCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                    "Vui lòng tìm kiếm và xác nhận khách hàng trước khi tiếp tục!");
            return false;
        }
        if (foundCustomer.isIs_blacklist()) {
            showBlacklistWarning(foundCustomer);
            return false;
        }
        draft.setSelectedCustomer(foundCustomer);
        return true;
    }

    //  HELPERS
    private void fillForm(Customers cus) {
        txtName.setText(cus.getFull_name() != null ? cus.getFull_name() : "");
        txtPhone.setText(cus.getPhone()   != null ? cus.getPhone()    : "");
        txtAddress.setText(cus.getAddress() != null ? cus.getAddress() : "");
    }

    private void clearForm() {
        txtName.clear();
        txtPhone.clear();
        txtAddress.clear();
    }

    private void showBlacklistWarning(Customers cus) {
        if (boxBlacklistWarning != null) {
            boxBlacklistWarning.setVisible(true);
            boxBlacklistWarning.setManaged(true);
        }
        if (lblBlacklistReason != null) {
            String reason = (cus.getBlacklist_reason() != null
                    && !cus.getBlacklist_reason().isBlank())
                    ? cus.getBlacklist_reason()
                    : "Không có lý do cụ thể.";
            lblBlacklistReason.setText("Lý do: " + reason);
        }
        if (boxBlacklistWarning == null) {
            showAlert(Alert.AlertType.ERROR,
                    "⛔ KHÁCH HÀNG BỊ CẤM THUÊ",
                    "Khách hàng \"" + cus.getFull_name()
                            + "\" đang trong danh sách đen!\n"
                            + "Lý do: " + (cus.getBlacklist_reason() != null
                            ? cus.getBlacklist_reason() : "Không rõ"));
        }
    }

    private void hideBlacklistWarning() {
        if (boxBlacklistWarning != null) {
            boxBlacklistWarning.setVisible(false);
            boxBlacklistWarning.setManaged(false);
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