package com.example.rentalcar.controller.contract.booking;

import com.example.rentalcar.bll.PriceBLL;
import com.example.rentalcar.dao.VoucherDAO;
import com.example.rentalcar.models.DepositType;
import com.example.rentalcar.models.DiscountType;
import com.example.rentalcar.models.Vouchers;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;  // FIX BUG 3: phải là HBox, không phải VBox

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Step3Controller – Bước 3: Chọn thời gian, đặt cọc và áp dụng voucher.
 *
 * FIX BUG 3: boxVoucherApplied khai báo đúng kiểu HBox (FXML dùng HBox)
 */
public class Step3Controller {

    // ── Thời gian ──────────────────────────────────────────────
    @FXML private DatePicker       dpStart;
    @FXML private DatePicker       dpEnd;
    @FXML private Spinner<Integer> spinStartHour;
    @FXML private Spinner<Integer> spinStartMin;
    @FXML private Spinner<Integer> spinEndHour;
    @FXML private Spinner<Integer> spinEndMin;
    @FXML private Label            lblTotalDays;
    @FXML private Label            lblTotalDuration;

    // ── Giá tiền ───────────────────────────────────────────────
    @FXML private Label lblBasePrice;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotalPrice;

    // ── Voucher ────────────────────────────────────────────────
    @FXML private TextField txtVoucherCode;
    @FXML private Label     lblVoucherMsg;
    // FIX BUG 3: FXML khai báo <HBox fx:id="boxVoucherApplied"> → phải là HBox
    @FXML private HBox      boxVoucherApplied;
    @FXML private Label     lblVoucherDetail;

    // ── Đặt cọc ────────────────────────────────────────────────
    @FXML private ComboBox<String> cbDepositType;
    @FXML private TextField        txtDepositAmount;

    // ── State ──────────────────────────────────────────────────
    private ContractDraft draft;
    private Vouchers      appliedVoucher;

    private final PriceBLL   priceBLL   = new PriceBLL();
    private final VoucherDAO voucherDAO = new VoucherDAO();

    // =========================================================
    //  NHẬN DRAFT
    // =========================================================
    public void setDraft(ContractDraft draft) {
        this.draft = draft;
        setupUI();

        // Khôi phục dữ liệu nếu quay lại từ bước 4
        if (draft.getStartDatetime() != null) {
            dpStart.setValue(draft.getStartDatetime().toLocalDate());
            if (spinStartHour != null)
                spinStartHour.getValueFactory().setValue(draft.getStartDatetime().getHour());
            if (spinStartMin != null)
                spinStartMin.getValueFactory().setValue(draft.getStartDatetime().getMinute());
        }
        if (draft.getEndDatetime() != null) {
            dpEnd.setValue(draft.getEndDatetime().toLocalDate());
            if (spinEndHour != null)
                spinEndHour.getValueFactory().setValue(draft.getEndDatetime().getHour());
            if (spinEndMin != null)
                spinEndMin.getValueFactory().setValue(draft.getEndDatetime().getMinute());
        }
        if (draft.getDepositType() != null) {
            cbDepositType.setValue(depositTypeToDisplay(draft.getDepositType()));
        }
        if (draft.getDepositAmount() > 0) {
            txtDepositAmount.setText(String.valueOf((long) draft.getDepositAmount()));
        }
        if (draft.getAppliedVoucher() != null) {
            appliedVoucher = draft.getAppliedVoucher();
            txtVoucherCode.setText(appliedVoucher.getCode_vouchers());
        }

        recalculate();
    }

    // =========================================================
    //  SETUP UI
    // =========================================================
    private void setupUI() {
        cbDepositType.getItems().addAll("Tiền mặt", "Giấy tờ", "Khác");
        cbDepositType.setValue("Tiền mặt");

        setupSpinner(spinStartHour, 0, 23, 8);
        setupSpinner(spinStartMin,  0, 59, 0);
        setupSpinner(spinEndHour,   0, 23, 8);
        setupSpinner(spinEndMin,    0, 59, 0);

        dpStart.setValue(LocalDate.now());
        dpEnd.setValue(LocalDate.now().plusDays(1));

        dpStart.valueProperty().addListener((obs, o, n) -> recalculate());
        dpEnd.valueProperty().addListener((obs, o, n)   -> recalculate());

        if (spinStartHour != null)
            spinStartHour.valueProperty().addListener((obs, o, n) -> recalculate());
        if (spinStartMin != null)
            spinStartMin.valueProperty().addListener((obs, o, n)  -> recalculate());
        if (spinEndHour != null)
            spinEndHour.valueProperty().addListener((obs, o, n)   -> recalculate());
        if (spinEndMin != null)
            spinEndMin.valueProperty().addListener((obs, o, n)    -> recalculate());

        // Ẩn box voucher đã áp dụng
        if (boxVoucherApplied != null) {
            boxVoucherApplied.setVisible(false);
            boxVoucherApplied.setManaged(false);
        }

        recalculate();
    }

    private void setupSpinner(Spinner<Integer> spinner, int min, int max, int init) {
        if (spinner == null) return;
        spinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, init));
        spinner.setEditable(true);
    }

    // =========================================================
    //  TÍNH TIỀN TỰ ĐỘNG
    // =========================================================
    private void recalculate() {
        if (draft == null || draft.getSelectedVehicle() == null) return;
        if (dpStart.getValue() == null || dpEnd.getValue() == null) return;

        LocalDateTime start = buildDateTime(dpStart, spinStartHour, spinStartMin);
        LocalDateTime end   = buildDateTime(dpEnd,   spinEndHour,   spinEndMin);

        if (!start.isBefore(end)) {
            setLabel(lblBasePrice,  "0 đ");
            setLabel(lblTotalPrice, "0 đ");
            setLabel(lblTotalDays,  "⚠  Ngày kết thúc phải sau ngày bắt đầu!");
            return;
        }

        double priceDay  = draft.getSelectedVehicle().getPrice_day();
        double priceHour = draft.getSelectedVehicle().getPrice_hour();
        double basePrice = priceBLL.calculateBasePrice(start, end, priceDay, priceHour);

        double discount = 0;
        if (appliedVoucher != null) {
            discount = calculateDiscount(basePrice, appliedVoucher);
        }

        double total = basePrice - discount;

        setLabel(lblBasePrice,  formatMoney(basePrice));
        setLabel(lblDiscount,   discount > 0 ? "- " + formatMoney(discount) : "0 đ");
        setLabel(lblTotalPrice, formatMoney(total));

        // Tổng thời gian
        long hours = java.time.Duration.between(start, end).toHours();
        long days  = hours / 24;
        long remH  = hours % 24;
        String dur = days > 0
                ? days + " ngày" + (remH > 0 ? " " + remH + " giờ" : "")
                : hours + " giờ";
        setLabel(lblTotalDays,     dur);
        setLabel(lblTotalDuration, dur);
    }

    // =========================================================
    //  VOUCHER
    // =========================================================
    @FXML
    void handleApplyVoucher() {
        String code = txtVoucherCode != null
                ? txtVoucherCode.getText().trim().toUpperCase() : "";
        if (code.isEmpty()) {
            setVoucherMsg("Vui lòng nhập mã voucher!", false);
            return;
        }

        try {
            Vouchers v = voucherDAO.findByCode(code);
            if (v == null) {
                setVoucherMsg("❌ Mã voucher không hợp lệ hoặc đã hết hạn!", false);
                appliedVoucher = null;
                hideVoucherApplied();
            } else if (v.getUsage_count() >= v.getUsage_limit()) {
                setVoucherMsg("❌ Mã voucher đã hết lượt sử dụng!", false);
                appliedVoucher = null;
                hideVoucherApplied();
            } else {
                appliedVoucher = v;
                setVoucherMsg("✅ Áp dụng thành công!", true);
                showVoucherApplied(v);
                recalculate();
            }
        } catch (Exception e) {
            setVoucherMsg("Lỗi kiểm tra voucher: " + e.getMessage(), false);
        }
    }

    @FXML
    void handleRemoveVoucher() {
        appliedVoucher = null;
        if (txtVoucherCode != null) txtVoucherCode.clear();
        setVoucherMsg("", true);
        hideVoucherApplied();
        recalculate();
    }

    private void showVoucherApplied(Vouchers v) {
        if (boxVoucherApplied == null || lblVoucherDetail == null) return;
        String detail = v.getDiscount_type() == DiscountType.CO_DINH
                ? "Giảm " + formatMoney(v.getDiscount_value())
                : "Giảm " + (int) v.getDiscount_value() + "%";
        lblVoucherDetail.setText(v.getCode_vouchers() + " – " + detail);
        boxVoucherApplied.setVisible(true);
        boxVoucherApplied.setManaged(true);
    }

    private void hideVoucherApplied() {
        if (boxVoucherApplied == null) return;
        boxVoucherApplied.setVisible(false);
        boxVoucherApplied.setManaged(false);
    }

    private void setVoucherMsg(String msg, boolean ok) {
        if (lblVoucherMsg == null) return;
        lblVoucherMsg.setText(msg);
        lblVoucherMsg.setStyle(ok
                ? "-fx-text-fill: #16a34a; -fx-font-size: 12px;"
                : "-fx-text-fill: #e11d48; -fx-font-size: 12px;");
        lblVoucherMsg.setVisible(!msg.isEmpty());
        lblVoucherMsg.setManaged(!msg.isEmpty());
    }

    // =========================================================
    //  VALIDATE & LƯU VÀO DRAFT
    // =========================================================
    public boolean validateAndSave() {
        if (dpStart.getValue() == null || dpEnd.getValue() == null) {
            showAlert("Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc!");
            return false;
        }

        LocalDateTime start = buildDateTime(dpStart, spinStartHour, spinStartMin);
        LocalDateTime end   = buildDateTime(dpEnd,   spinEndHour,   spinEndMin);

        if (!start.isBefore(end)) {
            showAlert("Ngày kết thúc phải sau ngày bắt đầu!");
            return false;
        }

        if (cbDepositType.getValue() == null) {
            showAlert("Vui lòng chọn hình thức đặt cọc!");
            return false;
        }

        draft.setStartDatetime(start);
        draft.setEndDatetime(end);
        draft.setDepositType(displayToDepositType(cbDepositType.getValue()));

        double deposit = 0;
        if (txtDepositAmount != null && !txtDepositAmount.getText().isBlank()) {
            try {
                deposit = Double.parseDouble(
                        txtDepositAmount.getText().trim()
                                .replace(",", "").replace(".", ""));
            } catch (NumberFormatException e) {
                showAlert("Số tiền cọc không hợp lệ!");
                return false;
            }
        }
        draft.setDepositAmount(deposit);
        draft.setAppliedVoucher(appliedVoucher);

        double priceDay  = draft.getSelectedVehicle().getPrice_day();
        double priceHour = draft.getSelectedVehicle().getPrice_hour();
        double base      = priceBLL.calculateBasePrice(start, end, priceDay, priceHour);
        double discount  = appliedVoucher != null ? calculateDiscount(base, appliedVoucher) : 0;

        draft.setBasePrice(base);
        draft.setDiscountAmount(discount);
        draft.setTotalPrice(base - discount);

        return true;
    }

    // =========================================================
    //  HELPERS
    // =========================================================
    private LocalDateTime buildDateTime(DatePicker dp,
                                        Spinner<Integer> hour,
                                        Spinner<Integer> min) {
        LocalDate date = dp.getValue();
        int h = (hour != null && hour.getValue() != null) ? hour.getValue() : 8;
        int m = (min  != null && min.getValue()  != null) ? min.getValue()  : 0;
        return LocalDateTime.of(date, LocalTime.of(h, m));
    }

    private double calculateDiscount(double base, Vouchers v) {
        if (v.getDiscount_type() == DiscountType.CO_DINH) {
            return Math.min(v.getDiscount_value(), base);
        } else {
            return base * v.getDiscount_value() / 100.0;
        }
    }

    private String depositTypeToDisplay(DepositType t) {
        return switch (t) {
            case TIEN_MAT -> "Tiền mặt";
            case GIAY_TO  -> "Giấy tờ";
            default       -> "Khác";
        };
    }

    private DepositType displayToDepositType(String s) {
        return switch (s) {
            case "Tiền mặt" -> DepositType.TIEN_MAT;
            case "Giấy tờ"  -> DepositType.GIAY_TO;
            default          -> DepositType.KHAC;
        };
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f đ", amount).replace(",", ".");
    }

    private void setLabel(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thiếu thông tin");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}