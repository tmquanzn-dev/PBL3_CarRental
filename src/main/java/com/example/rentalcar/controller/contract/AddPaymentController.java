package com.example.rentalcar.controller.contract;

import com.example.rentalcar.bll.PaymentBLL;
import com.example.rentalcar.models.*;
import com.example.rentalcar.utils.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * AddPaymentController – Cho phép Staff/Admin thu tiền từ hợp đồng.
 * Mở dưới dạng modal từ ContractManagementController.
 */
public class AddPaymentController {

    @FXML private Label     lblContractCode;
    @FXML private Label     lblCustomerName;
    @FXML private Label     lblTotalPrice;
    @FXML private Label     lblMsg;

    @FXML private ComboBox<String> cbPaymentType;
    @FXML private ComboBox<String> cbPaymentMethod;
    @FXML private TextField        txtAmount;

    private final PaymentBLL paymentBLL = new PaymentBLL();

    private Contracts contract;
    private Runnable  onSaved;

    // ─────────────────────────────────────────────────────
    //  NHẬN DỮ LIỆU
    // ─────────────────────────────────────────────────────
    public void setContract(Contracts contract, Runnable onSaved) {
        this.contract = contract;
        this.onSaved  = onSaved;
        fillInfo();
    }

    @FXML
    public void initialize() {
        cbPaymentType.getItems().addAll(
                "Tiền cọc",
                "Thanh toán phần còn lại",
                "Hoàn tiền",
                "Phụ thu"
        );
        cbPaymentType.setValue("Thanh toán phần còn lại");

        cbPaymentMethod.getItems().addAll("Tiền mặt", "Chuyển khoản");
        cbPaymentMethod.setValue("Tiền mặt");
    }

    private void fillInfo() {
        if (contract == null) return;
        setLabel(lblContractCode, "#" + contract.getCode_contract());
        setLabel(lblTotalPrice, fmt(contract.getTotal_price()));

        // Tên khách hàng
        if (contract.getId_customer() != null) {
            String name = contract.getId_customer().getFull_name();
            setLabel(lblCustomerName, name != null ? name : "--");
        }

        // Gợi ý số tiền = tổng tiền hợp đồng
        if (txtAmount != null && contract.getTotal_price() > 0) {
            txtAmount.setText(String.valueOf((long) contract.getTotal_price()));
        }
    }

    // ─────────────────────────────────────────────────────
    //  LƯU PAYMENT
    // ─────────────────────────────────────────────────────
    @FXML
    void handleSave() {
        // Validate số tiền
        double amount;
        try {
            String raw = txtAmount.getText().trim()
                    .replace(".", "").replace(",", "");
            amount = Double.parseDouble(raw);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showMsg("❌  Số tiền không hợp lệ!", false);
            return;
        }

        if (cbPaymentType.getValue() == null) {
            showMsg("❌  Vui lòng chọn loại thanh toán!", false);
            return;
        }
        if (cbPaymentMethod.getValue() == null) {
            showMsg("❌  Vui lòng chọn phương thức thanh toán!", false);
            return;
        }

        try {
            Payments payment = new Payments();
            payment.setAmount(amount);
            payment.setPayment_type(displayToPaymentType(cbPaymentType.getValue()));
            payment.setPayment_method(displayToPaymentMethod(cbPaymentMethod.getValue()));
            payment.setId_contract(contract);
            // id_user sẽ được set bởi PaymentBLL (AppSession.getCurrentUser())

            boolean ok = paymentBLL.createPayment(payment);
            if (ok) {
                showMsg("✅  Ghi nhận thanh toán thành công!", true);
                if (onSaved != null) onSaved.run();
                // Tự đóng sau 1 giây
                new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                                javafx.util.Duration.seconds(1.2),
                                e -> closeStage()
                        )
                ).play();
            } else {
                showMsg("❌  Không thể lưu giao dịch!", false);
            }

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showMsg("❌  " + ex.getMessage(), false);
        } catch (Exception ex) {
            showMsg("❌  Lỗi hệ thống: " + ex.getMessage(), false);
            ex.printStackTrace();
        }
    }

    @FXML
    void handleCancel() {
        closeStage();
    }

    // ─────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────
    private PaymentType displayToPaymentType(String s) {
        return switch (s) {
            case "Tiền cọc"                  -> PaymentType.TIEN_COC;
            case "Thanh toán phần còn lại"   -> PaymentType.THANH_TOAN_PHAN_CON_LAI;
            case "Hoàn tiền"                 -> PaymentType.HOAN_TIEN;
            case "Phụ thu"                   -> PaymentType.PHU_THU;
            default                          -> PaymentType.THANH_TOAN_PHAN_CON_LAI;
        };
    }

    private PaymentMethod displayToPaymentMethod(String s) {
        return switch (s) {
            case "Chuyển khoản" -> PaymentMethod.CHUYEN_KHOAN;
            default             -> PaymentMethod.TIEN_MAT;
        };
    }

    private void showMsg(String text, boolean success) {
        if (lblMsg == null) return;
        lblMsg.setText(text);
        lblMsg.setStyle(success
                ? "-fx-text-fill: #16a34a; -fx-font-size: 13px; -fx-font-weight: bold;"
                : "-fx-text-fill: #e11d48; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblMsg.setVisible(true);
        lblMsg.setManaged(true);
    }

    private void setLabel(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }

    private String fmt(double v) {
        return String.format("%,.0f đ", v).replace(",", ".");
    }

    private void closeStage() {
        Stage stage = (Stage) txtAmount.getScene().getWindow();
        stage.close();
    }
}