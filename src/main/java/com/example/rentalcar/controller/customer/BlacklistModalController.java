package com.example.rentalcar.controller.customer;

import com.example.rentalcar.bll.CustomerBLL;
import com.example.rentalcar.models.Customers;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class BlacklistModalController {
    @FXML private Label lblCustomerName;
    @FXML private Label lblCustomerCccd;
    @FXML private TextArea txtReason;
    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;
    @FXML private Label lblMsg;

    private final CustomerBLL customerBLL = new CustomerBLL();
    private Customers customer;
    private Consumer<Boolean> callback;

    public void setCustomer(Customers customer, Consumer<Boolean> callback) {
        this.customer = customer;
        this.callback = callback;

        if (lblCustomerName != null)
            lblCustomerName.setText(customer.getFull_name());
        if (lblCustomerCccd != null)
            lblCustomerCccd.setText("CCCD: " + customer.getCccd());
    }

    @FXML
    void handleConfirm() {
        String reason = txtReason != null ? txtReason.getText().trim() : "";
        if (reason.isBlank()) {
            showMsg("Vui lòng nhập lý do đưa vào danh sách đen!", false);
            return;
        }

        try {
            boolean ok = customerBLL.addToBlacklist(customer.getId_customer(), reason);
            if (ok) {
                if (callback != null)
                    callback.accept(true);
                closeStage();
            } else {
                showMsg("Thao tác thất bại, vui lòng thử lại!", false);
            }
        } catch (Exception e) {
            showMsg(e.getMessage(), false);
        }
    }

    @FXML
    void handleCancel() {
        if (callback != null) callback.accept(false);
        closeStage();
    }

    private void showMsg(String text, boolean ok) {
        if (lblMsg == null) return;
        lblMsg.setText(text);
        lblMsg.setStyle(ok
                ? "-fx-text-fill: #16a34a; -fx-font-weight: bold;"
                : "-fx-text-fill: #e11d48; -fx-font-weight: bold;");
        lblMsg.setVisible(true);
    }

    private void closeStage() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
