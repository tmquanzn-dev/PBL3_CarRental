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

/**
 * AddPaymentController – Điều khiển form Ghi nhận thanh toán.
 * ĐÃ ĐỒNG BỘ: Khớp 100% với các fx:id và sự kiện handleSave, handleCancel trong file FXML thực tế.
 * ĐÃ FIX: Chống lỗi NullPointerException khi insert giao dịch và nạp dữ liệu khách hàng.
 */
public class AddPaymentController {

    // Khai báo các thành phần giao diện khớp chính xác theo fx:id của file FXML
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
        // Nạp danh sách các hằng số từ Enum vào ComboBox Loại thanh toán
        if (cbPaymentType != null) {
            cbPaymentType.setItems(FXCollections.observableArrayList(PaymentType.values()));
            cbPaymentType.setValue(PaymentType.THANH_TOAN_PHAN_CON_LAI); // Đặt mặc định theo cấu trúc Enum của bạn
        }

        // Nạp danh sách các hằng số từ Enum vào ComboBox Phương thức thanh toán
        if (cbPaymentMethod != null) {
            cbPaymentMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
            cbPaymentMethod.setValue(PaymentMethod.TIEN_MAT);
        }
    }

    /**
     * Đồng bộ nhận dữ liệu Hợp đồng và Hàm callback tải lại bảng danh sách từ màn hình quản lý cha truyền qua
     */
    public void setContract(Contracts contract, Runnable refreshCallback) {
        if (contract == null) return;
        this.contract = contract;
        this.onRefreshCallback = refreshCallback;

        // Tải thông tin hợp đồng mới nhất cập nhật từ Database lên
        Contracts freshContract = contractDAO.findById(contract.getId_contract());
        if (freshContract != null) {
            this.contract = freshContract;
        }

        refreshDisplayData();
    }

    /**
     * Đổ dữ liệu hợp đồng lên các nhãn hiển thị của Form
     */
    private void refreshDisplayData() {
        if (contract == null) return;

        // 1. Hiển thị mã hợp đồng
        if (lblContractCode != null) {
            lblContractCode.setText("#" + contract.getCode_contract());
        }

        // 2. Tìm kiếm và nạp họ tên khách hàng từ Database dựa trên ID khách hàng
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

        // 3. Hiển thị tổng số tiền của hợp đồng tổng
        if (lblTotalPrice != null) {
            lblTotalPrice.setText(String.format("%,.0f đ", contract.getTotal_price()).replace(",", "."));
        }

        // 4. Điền gợi ý số tiền cần nhập bằng cách tính toán số tiền còn thiếu (nếu hệ thống của bạn cho phép tính toán tự động)
        if (txtAmount != null) {
            txtAmount.setText(String.format("%.0f", contract.getTotal_price()));
        }
    }

    /**
     * Xử lý hành động bấm nút "💾 Ghi nhận thanh toán"
     */
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

            // Khởi tạo thực thể Payments mới để lưu trữ
            Payments newPayment = new Payments();
            newPayment.setId_contract(contract);
            newPayment.setAmount(amountToPay);
            newPayment.setPayment_method(cbPaymentMethod.getValue());
            newPayment.setPayment_type(cbPaymentType.getValue());

            // ✅ FIX LỖI CRASH NULL POINTER EXCEPTION:
            // Đảm bảo gán chắc chắn thực thể người dùng, phòng hờ AppSession.getCurrentUser() trả về null khi mất dấu session
            if (AppSession.getCurrentUser() != null) {
                newPayment.setId_user(AppSession.getCurrentUser());
            } else if (contract.getId_user() != null) {
                newPayment.setId_user(contract.getId_user());
            } else {
                Users systemUser = new Users();
                systemUser.setId_user(1); // Gán ID mặc định cứu cánh của hệ thống để chống sập database
                newPayment.setId_user(systemUser);
            }

            // Gọi tầng nghiệp vụ lưu dữ liệu vào MySQL
            boolean isSaved = paymentBLL.createPayment(newPayment);

            if (isSaved) {
                // Cập nhật lại trạng thái tiền bạc của hợp đồng cha tương ứng
                contract.setPayment_status(PaymentStatus.DA_THANH_TOAN);
                contractDAO.update(contract);

                // Hiển thị thông báo thành công dạng Alert đẹp mắt
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Ghi nhận giao dịch thanh toán thành công!");
                alert.showAndWait();

                // ✅ KÍCH HOẠT CALLBACK: Báo ra ngoài màn hình quản lý cha làm mới dữ liệu TableView lập tức
                if (onRefreshCallback != null) {
                    onRefreshCallback.run();
                }

                // Tự động đóng cửa sổ modal form
                handleCancel();
            } else {
                showToast("Không thể lưu giao dịch vào cơ sở dữ liệu!", true);
            }

        } catch (NumberFormatException e) {
            showToast("Số tiền nhập vào không hợp lệ! Chỉ cho phép nhập số.", true);
        }
    }

    /**
     * Xử lý hành động bấm nút "Hủy" hoặc đóng cửa sổ
     */
    @FXML
    private void handleCancel() {
        if (lblContractCode != null && lblContractCode.getScene() != null) {
            Stage stage = (Stage) lblContractCode.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Hàm phụ trợ hiển thị thông báo nhanh (Toast Message) trên giao diện Form
     */
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