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
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Step1Controller – Bước 1: Tìm kiếm & xác nhận thông tin khách hàng.
 *
 * Chức năng:
 *  1. Tìm khách theo CCCD → điền tự động form.
 *  2. Kiểm tra BLACKLIST → chặn ngay, hiện lý do, không cho tiếp tục.
 *  3. Khách mới → mở modal AddCustomerModal, sau đó tự điền lại.
 *  4. validateAndSave() → lưu Customers vào ContractDraft.
 *
 * Đường dẫn: src/main/java/com/example/rentalcar/controller/contract/booking/Step1Controller.java
 */
public class Step1Controller {

    // =========================================================
    //  FXML
    // =========================================================
    @FXML private TextField txtSearchCCCD;
    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtAddress;

    // Box hiển thị cảnh báo blacklist
    @FXML private HBox  boxBlacklistWarning;
    @FXML private Label lblBlacklistReason;

    // =========================================================
    //  STATE
    // =========================================================
    private ContractDraft draft;
    private Customers     foundCustomer; // Khách đang được chọn

    private final CustomerBLL customerBLL = new CustomerBLL();

    // =========================================================
    //  NHẬN DRAFT TỪ PARENT CONTROLLER
    // =========================================================
    public void setDraft(ContractDraft draft) {
        this.draft = draft;

        // Nếu quay lại từ bước 2 → điền lại dữ liệu đã có
        if (draft.getSelectedCustomer() != null) {
            fillForm(draft.getSelectedCustomer());
            foundCustomer = draft.getSelectedCustomer();
        }
    }

    // =========================================================
    //  TÌM KIẾM THEO CCCD
    // =========================================================
    @FXML
    void handleSearchCCCD() {
        String cccd = txtSearchCCCD.getText().trim();
        if (cccd.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số CCCD để tìm kiếm!");
            return;
        }

        // Ẩn cảnh báo blacklist cũ
        hideBlacklistWarning();

        try {
            Customers cus = customerBLL.findByCccd(cccd);

            if (cus != null) {
                // ── KIỂM TRA BLACKLIST ──────────────────────────────
                if (cus.isIs_blacklist()) {
                    showBlacklistWarning(cus);
                    clearForm();
                    foundCustomer = null;
                    return;
                }

                // Khách bình thường → điền form
                fillForm(cus);
                foundCustomer = cus;

            } else {
                // Không tìm thấy → gợi ý thêm mới
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

    // =========================================================
    //  VALIDATE & LƯU VÀO DRAFT (gọi bởi CreateContractController)
    // =========================================================
    public boolean validateAndSave() {
        if (foundCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                    "Vui lòng tìm kiếm và xác nhận khách hàng trước khi tiếp tục!");
            return false;
        }

        // Double-check blacklist lần nữa trước khi tiếp
        if (foundCustomer.isIs_blacklist()) {
            showBlacklistWarning(foundCustomer);
            return false;
        }

        draft.setSelectedCustomer(foundCustomer);
        return true;
    }

    // =========================================================
    //  HELPERS
    // =========================================================

    /** Điền dữ liệu khách hàng vào form */
    private void fillForm(Customers cus) {
        txtName.setText(cus.getFull_name());
        txtPhone.setText(cus.getPhone() != null ? cus.getPhone() : "");
        txtAddress.setText(cus.getAddress() != null ? cus.getAddress() : "");
    }

    /** Xóa sạch form */
    private void clearForm() {
        txtName.clear();
        txtPhone.clear();
        txtAddress.clear();
    }

    /** Hiện banner cảnh báo blacklist với lý do */
    private void showBlacklistWarning(Customers cus) {
        if (boxBlacklistWarning != null) {
            boxBlacklistWarning.setVisible(true);
            boxBlacklistWarning.setManaged(true);
        }
        if (lblBlacklistReason != null) {
            String reason = (cus.getBlacklist_reason() != null && !cus.getBlacklist_reason().isBlank())
                    ? cus.getBlacklist_reason()
                    : "Không có lý do cụ thể.";
            lblBlacklistReason.setText("Lý do: " + reason);
        }

        // Fallback: nếu FXML chưa có box thì dùng Alert
        if (boxBlacklistWarning == null) {
            showAlert(Alert.AlertType.ERROR,
                    "⛔ KHÁCH HÀNG BỊ CẤM THUÊ",
                    "Khách hàng \"" + cus.getFull_name() + "\" đang trong danh sách đen!\n" +
                            "Lý do: " + (cus.getBlacklist_reason() != null ? cus.getBlacklist_reason() : "Không rõ"));
        }
    }

    /** Ẩn banner cảnh báo blacklist */
    private void hideBlacklistWarning() {
        if (boxBlacklistWarning != null) {
            boxBlacklistWarning.setVisible(false);
            boxBlacklistWarning.setManaged(false);
        }
    }

    /** Mở modal thêm khách mới, sau khi đóng tự tìm lại */
    private void openAddCustomerModal(String cccd) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/customer/AddCustomerModal.fxml"));
            Parent root = loader.load();

            AddCustomerController ctrl = loader.getController();
            ctrl.setPreFillCccd(cccd);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Thêm khách hàng mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Sau khi đóng modal → tìm lại khách vừa thêm
            Customers newCus = customerBLL.findByCccd(cccd);
            if (newCus != null && !newCus.isIs_blacklist()) {
                fillForm(newCus);
                foundCustomer = newCus;
                hideBlacklistWarning();
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