package com.example.rentalcar.controller.contract.booking;

import com.example.rentalcar.dao.ContractDAO;
import com.example.rentalcar.dao.VehicleDAO;
import com.example.rentalcar.dao.VoucherDAO;
import com.example.rentalcar.models.*;
import com.example.rentalcar.utils.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Step4Controller – Bước 4: Xác nhận tóm tắt và lưu hợp đồng xuống DB.
 *
 * Chức năng:
 *  1. Hiển thị tóm tắt toàn bộ thông tin từ ContractDraft.
 *  2. confirmAndSave() → INSERT vào bảng contracts.
 *  3. Sau khi INSERT → UPDATE status xe thành RENTED.
 *  4. Nếu có voucher → tăng usage_count lên 1.
 *  5. Gọi callback onSaved để đóng cửa sổ.
 *
 * Đường dẫn: src/main/java/com/example/rentalcar/controller/contract/booking/Step4Controller.java
 */
public class Step4Controller {

    // =========================================================
    //  FXML – Thông tin khách hàng & xe
    // =========================================================
    @FXML private Label lblCustomerName;
    @FXML private Label lblCustomerPhone;
    @FXML private Label lblCustomerCccd;
    @FXML private Label lblVehicleName;
    @FXML private Label lblVehiclePlate;
    @FXML private Label lblVehicleType;

    // =========================================================
    //  FXML – Chi tiết thanh toán
    // =========================================================
    @FXML private Label lblRentalPeriod;
    @FXML private Label lblPricePerDay;
    @FXML private Label lblBasePrice;
    @FXML private Label lblDiscount;
    @FXML private Label lblVoucherCode;
    @FXML private Label lblDeposit;
    @FXML private Label lblFinalAmount;

    // =========================================================
    //  FXML – Ghi chú
    // =========================================================
    @FXML private TextArea txtNote;

    // =========================================================
    //  STATE
    // =========================================================
    private ContractDraft draft;
    private Runnable      onSaved; // Callback đóng cửa sổ

    private final ContractDAO contractDAO = new ContractDAO();
    private final VehicleDAO  vehicleDAO  = new VehicleDAO();
    private final VoucherDAO  voucherDAO  = new VoucherDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================================
    //  NHẬN DRAFT & CALLBACK
    // =========================================================
    public void setDraft(ContractDraft draft) {
        this.draft = draft;
        fillSummary();
    }

    public void setOnSaved(Runnable callback) {
        this.onSaved = callback;
    }

    // =========================================================
    //  ĐIỀN TÓM TẮT
    // =========================================================
    private void fillSummary() {
        if (draft == null) return;

        // Thông tin khách hàng
        Customers c = draft.getSelectedCustomer();
        if (c != null) {
            setLabel(lblCustomerName,  c.getFull_name());
            setLabel(lblCustomerPhone, c.getPhone() != null ? c.getPhone() : "--");
            setLabel(lblCustomerCccd,  c.getCccd());
        }

        // Thông tin xe
        Vehicles v = draft.getSelectedVehicle();
        if (v != null) {
            setLabel(lblVehicleName,  v.getBrand() + " " + v.getModel());
            setLabel(lblVehiclePlate, v.getCode_vehicle());
            setLabel(lblVehicleType,  v.getVehicle_type() != null ? v.getVehicle_type() : "--");
            setLabel(lblPricePerDay,  formatMoney(v.getPrice_day()) + "/ngày  |  "
                    + formatMoney(v.getPrice_hour()) + "/giờ");
        }

        // Thời gian thuê
        if (draft.getStartDatetime() != null && draft.getEndDatetime() != null) {
            setLabel(lblRentalPeriod,
                    draft.getStartDatetime().format(FMT) + "  →  " + draft.getEndDatetime().format(FMT));
        }

        // Giá tiền
        setLabel(lblBasePrice,   formatMoney(draft.getBasePrice()));
        setLabel(lblFinalAmount, formatMoney(draft.getTotalPrice()));

        if (draft.getDiscountAmount() > 0) {
            setLabel(lblDiscount, "- " + formatMoney(draft.getDiscountAmount()));
            if (draft.getAppliedVoucher() != null && lblVoucherCode != null) {
                lblVoucherCode.setText("(" + draft.getAppliedVoucher().getCode_vouchers() + ")");
                lblVoucherCode.setVisible(true);
            }
        } else {
            setLabel(lblDiscount, "0 đ");
        }

        // Đặt cọc
        if (draft.getDepositAmount() > 0) {
            String depositTypeStr = draft.getDepositType() != null
                    ? switch (draft.getDepositType()) {
                case TIEN_MAT -> "Tiền mặt";
                case GIAY_TO  -> "Giấy tờ";
                default       -> "Khác";
            }
                    : "";
            setLabel(lblDeposit, formatMoney(draft.getDepositAmount()) + " (" + depositTypeStr + ")");
        } else {
            setLabel(lblDeposit, "Không đặt cọc");
        }
    }

    // =========================================================
    //  XÁC NHẬN & LƯU XUỐNG DB
    // =========================================================
    public void confirmAndSave() {
        if (draft == null) return;

        // Lưu ghi chú vào draft nếu có
        if (txtNote != null && !txtNote.getText().isBlank()) {
            draft.setNote(txtNote.getText().trim());
        }

        try {
            // 1. Sinh mã hợp đồng
            List<Contracts> all = contractDAO.findAll();
            String code = ContractDraft.generateContractCode(all.size());

            // 2. Tạo object Contracts
            Contracts contract = buildContractFromDraft(code);

            // 3. INSERT xuống DB
            boolean saved = contractDAO.insert(contract);
            if (!saved) {
                showError("Không thể lưu hợp đồng. Vui lòng thử lại!");
                return;
            }

            // 4. Cập nhật trạng thái xe → RENTED
            Vehicles vehicle = draft.getSelectedVehicle();
            vehicle.setStatus(StatusVehicle.RENTED);
            vehicleDAO.update(vehicle);

            // 5. Tăng usage_count của voucher nếu có dùng
            if (draft.getAppliedVoucher() != null) {
                Vouchers v = draft.getAppliedVoucher();
                v.setUsage_count(v.getUsage_count() + 1);
                voucherDAO.update(v);
            }

            // 6. Thông báo thành công
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Tạo hợp đồng thành công");
            success.setHeaderText(null);
            success.setContentText("✅ Hợp đồng " + code + " đã được tạo thành công!\n"
                    + "Xe " + vehicle.getCode_vehicle() + " đã chuyển sang trạng thái đang thuê.");
            success.showAndWait();

            // 7. Gọi callback đóng cửa sổ
            if (onSaved != null) onSaved.run();

        } catch (Exception e) {
            showError("Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================
    //  BUILD CONTRACT OBJECT TỪ DRAFT
    // =========================================================
    private Contracts buildContractFromDraft(String code) {
        Contracts contract = new Contracts();

        contract.setCode_contract(code);
        contract.setStart_datetime(draft.getStartDatetime());
        contract.setEnd_datetime(draft.getEndDatetime());
        contract.setReturn_datetime(null);

        contract.setKm_start(draft.getKmStart());
        contract.setKm_end(0);
        contract.setFuel_start(draft.getFuelStart());
        contract.setFuel_end(0);

        contract.setDeposit_type(draft.getDepositType() != null
                ? draft.getDepositType() : DepositType.TIEN_MAT);
        contract.setDeposit_amount(draft.getDepositAmount());

        contract.setBase_price(draft.getBasePrice());
        contract.setDiscount_amount(draft.getDiscountAmount());
        contract.setTotal_price(draft.getTotalPrice());

        // Trạng thái mặc định khi mới tạo
        contract.setStatus(StatusContracts.DANG_THUE);
        contract.setPayment_status(PaymentStatus.CHUA_THANH_TOAN);

        // Nhân viên tạo HĐ = người đang đăng nhập
        contract.setId_user(AppSession.getCurrentUser());

        // Xe & Khách hàng
        contract.setId_vehicle(draft.getSelectedVehicle());
        contract.setId_customer(draft.getSelectedCustomer());

        // Voucher (có thể null)
        contract.setId_voucher(draft.getAppliedVoucher());

        return contract;
    }

    // =========================================================
    //  HELPERS
    // =========================================================
    private void setLabel(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f đ", amount).replace(",", ".");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}