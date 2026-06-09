package com.example.rentalcar.controller.contract;

import com.example.rentalcar.bll.PaymentBLL;
import com.example.rentalcar.dao.ContractDAO;
import com.example.rentalcar.dao.CustomerDAO;
import com.example.rentalcar.models.*;
import com.example.rentalcar.utils.AppSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddPaymentController {
    @FXML private Label lblContractCode;
    @FXML private Label lblCustomerName;
    @FXML private Label lblTotalPrice;
    @FXML private TextField txtAmount;
    @FXML private ComboBox<PaymentType> cbPaymentType;
    @FXML private ComboBox<PaymentMethod> cbPaymentMethod;
    @FXML private Label lblMsg;

    private Contracts contract;
    private Runnable onRefreshCallback; // Hàm callback dùng để tải lại bảng danh sách ở màn hình cha

    private final PaymentBLL paymentBLL = new PaymentBLL();
    private final ContractDAO contractDAO = new ContractDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    @FXML
    public void initialize() {
        if (cbPaymentType != null) {
            cbPaymentType.setItems(FXCollections.observableArrayList(PaymentType.values()));
            cbPaymentType.setValue(PaymentType.THANH_TOAN_PHAN_CON_LAI);
        }

        if (cbPaymentMethod != null) {
            cbPaymentMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
            cbPaymentMethod.setValue(PaymentMethod.TIEN_MAT);
        }
    }


    public void setContract(Contracts contract, Runnable refreshCallback) {
        if (contract == null) return;
        this.contract = contract;
        this.onRefreshCallback = refreshCallback;

        Contracts freshContract = contractDAO.findById(contract.getId_contract());
        if (freshContract != null) {
            this.contract = freshContract;
        }

        refreshDisplayData();
    }


    private void refreshDisplayData() {
        if (contract == null) return;

        if (lblContractCode != null) {
            lblContractCode.setText("#" + contract.getCode_contract());
        }

        if (lblCustomerName != null) {
            if (contract.getId_customer() != null) {
                Customers customer = customerDAO.findById(contract.getId_customer().getId_customer());
                if (customer != null && customer.getFull_name() != null) {
                    lblCustomerName.setText(customer.getFull_name());
                } else {
                    lblCustomerName.setText("Chưa xác định");
                }
            } else {
                lblCustomerName.setText("---");
            }
        }

        if (lblTotalPrice != null) {
            lblTotalPrice.setText(String.format("%,.0f đ", contract.getTotal_price()).replace(",", "."));
        }

        if (txtAmount != null) {
            txtAmount.setText(String.format("%.0f", contract.getTotal_price()));
        }
    }

    @FXML
    private void handleSave() {
        if (contract == null) {
            showToast("Lỗi: Dữ liệu hợp đồng rỗng!", true);
            return;
        }

        String amountText = txtAmount.getText().trim();
        if (amountText.isEmpty()) {
            showToast("Vui lòng nhập số tiền thanh toán!", true);
            return;
        }

        try {
            double amountToPay = Double.parseDouble(amountText);
            if (amountToPay <= 0) {
                showToast("Số tiền thanh toán phải lớn hơn 0 đ!", true);
                return;
            }

            Payments newPayment = new Payments();
            newPayment.setId_contract(contract);
            newPayment.setAmount(amountToPay);
            newPayment.setPayment_method(cbPaymentMethod.getValue());
            newPayment.setPayment_type(cbPaymentType.getValue());

            if (AppSession.getCurrentUser() != null) {
                newPayment.setId_user(AppSession.getCurrentUser());
            } else if (contract.getId_user() != null) {
                newPayment.setId_user(contract.getId_user());
            } else {
                Users systemUser = new Users();
                systemUser.setId_user(1);
                newPayment.setId_user(systemUser);
            }

            boolean isSaved = paymentBLL.createPayment(newPayment);

            if (isSaved) {
                contract.setPayment_status(PaymentStatus.DA_THANH_TOAN);
                contractDAO.update(contract);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Ghi nhận giao dịch thanh toán thành công!");
                alert.showAndWait();

                if (onRefreshCallback != null) {
                    onRefreshCallback.run();
                }

                handleCancel();
            } else {
                showToast("Không thể lưu giao dịch vào cơ sở dữ liệu!", true);
            }

        } catch (NumberFormatException e) {
            showToast("Số tiền nhập vào không hợp lệ! Chỉ cho phép nhập số.", true);
        }
    }

    @FXML
    private void handleCancel() {
        if (lblContractCode != null && lblContractCode.getScene() != null) {
            Stage stage = (Stage) lblContractCode.getScene().getWindow();
            stage.close();
        }
    }

    private void showToast(String message, boolean isError) {
        if (lblMsg == null) return;
        lblMsg.setText(message);
        if (isError) {
            lblMsg.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        } else {
            lblMsg.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        }
        lblMsg.setVisible(true);
        lblMsg.setManaged(true);
    }
}