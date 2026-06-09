package com.example.rentalcar.controller.contract.booking;

import com.example.rentalcar.bll.InspectionBLL;
import com.example.rentalcar.dao.ContractDAO;
import com.example.rentalcar.dao.CustomerDAO;
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

public class Step4Controller {

    @FXML private Label lblCustomerName;
    @FXML private Label lblCustomerPhone;
    @FXML private Label lblCustomerCccd;
    @FXML private Label lblVehicleName;
    @FXML private Label lblVehiclePlate;
    @FXML private Label lblVehicleType;
    @FXML private Label lblRentalPeriod;
    @FXML private Label lblPricePerDay;
    @FXML private Label lblBasePrice;
    @FXML private Label lblDiscount;
    @FXML private Label lblVoucherCode;
    @FXML private Label lblDeposit;
    @FXML private Label lblFinalAmount;
    @FXML private TextArea txtNote;

    private ContractDraft draft;
    private Runnable onSaved;

    private final ContractDAO contractDAO = new ContractDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final VoucherDAO voucherDAO = new VoucherDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final InspectionBLL  inspectionBLL  = new InspectionBLL();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setDraft(ContractDraft draft) {
        this.draft = draft;
        fillSummary();
    }

    public void setOnSaved(Runnable callback) {

        this.onSaved = callback;
    }

    private void fillSummary() {
        if (draft == null)
            return;

        Customers c = draft.getSelectedCustomer();
        if (c != null) {
            setLabel(lblCustomerName,  c.getFull_name());
            setLabel(lblCustomerPhone, c.getPhone() != null ? c.getPhone() : "--");
            setLabel(lblCustomerCccd,  c.getCccd());
        }

        Vehicles v = draft.getSelectedVehicle();
        if (v != null) {
            setLabel(lblVehicleName,  v.getBrand() + " " + v.getModel());
            setLabel(lblVehiclePlate, v.getCode_vehicle());
            setLabel(lblVehicleType,  v.getVehicle_type() != null ? v.getVehicle_type() : "--");
            setLabel(lblPricePerDay,  formatMoney(v.getPrice_day()) + "/ngày  |  "
                    + formatMoney(v.getPrice_hour()) + "/giờ");
        }

        if (draft.getStartDatetime() != null && draft.getEndDatetime() != null) {
            setLabel(lblRentalPeriod,
                    draft.getStartDatetime().format(FMT) + "  →  " + draft.getEndDatetime().format(FMT));
        }

        setLabel(lblBasePrice, formatMoney(draft.getBasePrice()));
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

        if (draft.getDepositAmount() > 0) {
            String depositTypeStr = draft.getDepositType() != null
                    ? switch (draft.getDepositType()) {
                case TIEN_MAT -> "Tiền mặt";
                case GIAY_TO  -> "Giấy tờ";
                default       -> "Khác";
            } : "";
            setLabel(lblDeposit, formatMoney(draft.getDepositAmount()) + " (" + depositTypeStr + ")");
        } else if (draft.getDepositType() == DepositType.GIAY_TO) {
            String noteStr = draft.getNote() != null ? draft.getNote() : "";
            setLabel(lblDeposit, "Giấy tờ  " + noteStr);
        } else {
            setLabel(lblDeposit, "Không đặt cọc");
        }

        if (txtNote != null && draft.getNote() != null && !draft.getNote().isBlank()) {
            txtNote.setText(draft.getNote());
        }
    }

    public void confirmAndSave() {
        if (draft == null)
            return;

        if (txtNote != null && !txtNote.getText().isBlank()) {
            String existingNote = draft.getNote() != null ? draft.getNote() : "";
            String userNote = txtNote.getText().trim();
            if (!existingNote.isEmpty() && !existingNote.equals(userNote)) {
                draft.setNote(existingNote + "\n" + userNote);
            } else {
                draft.setNote(userNote);
            }
        }

        try {
            String code = generateUniqueContractCode();
            Contracts contract = buildContractFromDraft(code);

            boolean saved = contractDAO.insert(contract);
            if (!saved) {
                showError("Không thể lưu hợp đồng. Vui lòng thử lại!");
                return;
            }

            Vehicles vehicle = draft.getSelectedVehicle();
            vehicle.setStatus(StatusVehicle.RENTED);
            vehicleDAO.update(vehicle);

            if (draft.getAppliedVoucher() != null) {
                Vouchers v = draft.getAppliedVoucher();
                v.setUsage_count(v.getUsage_count() + 1);
                voucherDAO.update(v);
            }

            if (draft.getSelectedCustomer() != null) {
                customerDAO.incrementRentalCount(draft.getSelectedCustomer().getId_customer());
            }

            Contracts savedContract = contractDAO.findByCode(code);
            if (savedContract != null) {
                Inspections inspection = new Inspections();
                inspection.setId_contract(savedContract);
                inspection.setId_user(AppSession.getCurrentUser());
                inspection.setInspection_type(InspectionType.GIAO_XE);
                inspectionBLL.createInspection(inspection);
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Tạo hợp đồng thành công");
            success.setHeaderText(null);
            success.setContentText("✅ Hợp đồng " + code + " đã được tạo thành công!\n"
                    + "Xe " + vehicle.getCode_vehicle() + " đã chuyển sang trạng thái đang thuê.\n"
                    + "📋 Biên bản giao xe đã được lập tự động.");
            success.showAndWait();

            if (onSaved != null) onSaved.run();

        } catch (Exception e) {
            showError("Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateUniqueContractCode() {
        String datePart = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randPart = String.format("%04d", System.currentTimeMillis() % 10000);
        String baseCode = "HD-" + datePart + "-" + randPart;

        String finalCode = baseCode;
        int suffix = 1;
        while (contractDAO.isCodeExists(finalCode)) {
            finalCode = baseCode + "-" + suffix;
            suffix++;
        }
        return finalCode;
    }


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

        contract.setStatus(StatusContracts.DANG_THUE);
        contract.setPayment_status(PaymentStatus.CHUA_THANH_TOAN);

        contract.setId_user(AppSession.getCurrentUser());
        contract.setId_vehicle(draft.getSelectedVehicle());
        contract.setId_customer(draft.getSelectedCustomer());
        contract.setId_voucher(draft.getAppliedVoucher());

        return contract;
    }

    //  HELPERS
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