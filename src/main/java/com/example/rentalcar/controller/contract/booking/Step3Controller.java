package com.example.rentalcar.controller.contract.booking;

import com.example.rentalcar.bll.PriceBLL;
import com.example.rentalcar.bll.RuleBLL;
import com.example.rentalcar.dao.VoucherDAO;
import com.example.rentalcar.models.DepositType;
import com.example.rentalcar.models.DiscountType;
import com.example.rentalcar.models.Vouchers;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


public class Step3Controller {
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private Spinner<Integer> spinStartHour;
    @FXML private Spinner<Integer> spinStartMin;
    @FXML private Spinner<Integer> spinEndHour;
    @FXML private Spinner<Integer> spinEndMin;
    @FXML private Label lblTotalDays;
    @FXML private Label lblTotalDuration;

    @FXML private Label lblBasePrice;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotalPrice;

    @FXML private TextField txtVoucherCode;
    @FXML private Label lblVoucherMsg;
    @FXML private HBox boxVoucherApplied;
    @FXML private Label lblVoucherDetail;

    // ── Đặt cọc ────────────────────────────────────────────────
    @FXML private ComboBox<String> cbDepositType;
    @FXML private TextField txtDepositAmount;

    // ── State ──────────────────────────────────────────────────
    private ContractDraft draft;
    private Vouchers appliedVoucher;

    private final PriceBLL priceBLL = new PriceBLL();
    private final RuleBLL ruleBLL = new RuleBLL();
    private final VoucherDAO voucherDAO = new VoucherDAO();

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

    private void setupUI() {
        cbDepositType.getItems().clear();
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

    private void recalculate() {
        if (draft == null || draft.getSelectedVehicle() == null) return;
        if (dpStart.getValue() == null || dpEnd.getValue() == null) return;

        LocalDateTime start = buildDateTime(dpStart, spinStartHour, spinStartMin);
        if (start.isBefore(LocalDateTime.now())) {
            setLabel(lblBasePrice, "0 đ");
            setLabel(lblDiscount, "0 đ");
            setLabel(lblTotalPrice, "0 đ");
            setLabel(lblTotalDays, "⚠ Thời gian bắt đầu không hợp lệ!");
            setLabel(lblTotalDuration, "");
            return;
        }

        LocalDateTime end = buildDateTime(dpEnd, spinEndHour, spinEndMin);
        if (!start.isBefore(end)) {
            setLabel(lblBasePrice, "0 đ");
            setLabel(lblDiscount, "0 đ");
            setLabel(lblTotalPrice, "0 đ");
            setLabel(lblTotalDays, "⚠ Ngày kết thúc phải sau ngày bắt đầu!");
            setLabel(lblTotalDuration, "");
            return;
        }

        double[] prices = calculateCurrentPrice(start, end);

        setLabel(lblBasePrice,  formatMoney(prices[0]));
        setLabel(lblDiscount,   prices[1] > 0 ? "- " + formatMoney(prices[1]) : "0 đ");
        setLabel(lblTotalPrice, formatMoney(prices[2]));

        // Tổng thời gian hiển thị nhãn UI
        long hours = java.time.Duration.between(start, end).toHours();
        long daysCount  = hours / 24;
        long remH  = hours % 24;
        String dur = daysCount > 0
                ? daysCount + " ngày" + (remH > 0 ? " " + remH + " giờ" : "")
                : hours + " giờ";
        setLabel(lblTotalDays,     dur);
        setLabel(lblTotalDuration, dur);
    }
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

    public boolean validateAndSave() {
        if (dpStart.getValue() == null || dpEnd.getValue() == null) {
            showAlert("Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc!");
            return false;
        }

        LocalDateTime start = buildDateTime(dpStart, spinStartHour, spinStartMin);
        if (start.isBefore(LocalDateTime.now())) {
            showAlert("Thời gian bắt đầu không được nằm trong quá khứ!");
            return false;
        }

        LocalDateTime end = buildDateTime(dpEnd, spinEndHour, spinEndMin);
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
                deposit = Double.parseDouble(txtDepositAmount.getText().trim().replace(",", "").replace(".", ""));
            } catch (NumberFormatException e) {
                showAlert("Số tiền cọc không hợp lệ!");
                return false;
            }
        }
        draft.setDepositAmount(deposit);
        draft.setAppliedVoucher(appliedVoucher);

        double[] prices = calculateCurrentPrice(start, end);
        draft.setBasePrice(prices[0]);
        draft.setDiscountAmount(prices[1]);
        draft.setTotalPrice(prices[2]);

        return true;
    }

    private double[] calculateCurrentPrice(LocalDateTime start, LocalDateTime end) {
        if (draft == null || draft.getSelectedVehicle() == null || start == null || end == null || !start.isBefore(end)) {
            return new double[]{0.0, 0.0, 0.0};
        }

        java.time.Duration duration = java.time.Duration.between(start, end);
        long totalHours = duration.toHours();
        double basePrice = 0.0;

        double priceDay  = draft.getSelectedVehicle().getPrice_day();
        double priceHour = draft.getSelectedVehicle().getPrice_hour();

        if (totalHours < 24) {
            double baseAmount = totalHours * priceHour;
            double multiplier = ruleBLL.getSmartMultiplierForDate(start.toLocalDate());
            basePrice = baseAmount * multiplier;
        } else {
            long days = totalHours / 24;
            long remainingHours = totalHours % 24;
            LocalDate startLocalDate = start.toLocalDate();

            for (int i = 0; i < days; i++) {
                LocalDate currentDay = startLocalDate.plusDays(i);
                double dayMultiplier = ruleBLL.getSmartMultiplierForDate(currentDay);
                basePrice += (priceDay * dayMultiplier);
            }

            if (remainingHours > 0) {
                double hourMultiplier = ruleBLL.getSmartMultiplierForDate(end.toLocalDate());
                basePrice += (remainingHours * priceHour * hourMultiplier);
            }
        }

        double discount = appliedVoucher != null ? calculateDiscount(basePrice, appliedVoucher) : 0.0;
        double total = basePrice - discount;

        return new double[]{basePrice, discount, total};
    }

    //  HELPERS
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