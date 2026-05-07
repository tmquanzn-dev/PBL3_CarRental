package com.example.rentalcar.controller.contract;

import com.example.rentalcar.bll.ContractBLL;
import com.example.rentalcar.bll.InspectionBLL;
import com.example.rentalcar.bll.PartPriceBLL;
import com.example.rentalcar.bll.PenaltyBLL;
import com.example.rentalcar.dao.CustomerDAO;
import com.example.rentalcar.dao.VehicleDAO;
import com.example.rentalcar.models.*;
import com.example.rentalcar.utils.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReturnVehicleController {

    // ── Thông tin hợp đồng ────────────────────────────────────
    @FXML private Label lblContractCode;
    @FXML private Label lblCustomerName;
    @FXML private Label lblVehicleInfo;
    @FXML private Label lblPlannedReturn;

    // ── Thời gian trả ─────────────────────────────────────────
    @FXML private DatePicker dpReturnDate;
    @FXML private Spinner<Integer> spinReturnHour;
    @FXML private Spinner<Integer> spinReturnMin;

    // ── Xăng & KM ─────────────────────────────────────────────
    @FXML private Slider sliderFuel;
    @FXML private Label lblFuelPercent;
    @FXML private TextField txtKmEnd;

    // ── Tổng kết tiền ─────────────────────────────────────────
    @FXML private Label lblBasePrice;
    @FXML private Label lblLatePenalty;
    @FXML private Label lblFuelPenalty;
    @FXML private Label lblDamagePenalty;   // THÊM MỚI: phạt hư hỏng
    @FXML private Label lblFinalTotal;

    // ── Cảnh báo trễ ──────────────────────────────────────────
    @FXML private HBox  boxLateWarning;
    @FXML private Label lblLateMsg;

    // ── Thanh toán ────────────────────────────────────────────
    @FXML private ComboBox<String> cbPaymentStatus;

    // ── PHẦN HƯ HỎNG (THÊM MỚI) ──────────────────────────────
    @FXML private VBox  boxDamageSection;       // container section hư hỏng
    @FXML private CheckBox chkDamage;           // có hư hỏng không?
    @FXML private VBox  boxDamageDetails;       // hiện khi tick chkDamage
    @FXML private ListView<String> listDamageParts;  // danh sách phụ tùng hư
    @FXML private Label lblDamageTotal;         // tổng tiền phạt hư hỏng

    // ── BLL & DAO ─────────────────────────────────────────────
    private final ContractBLL    contractBLL    = new ContractBLL();
    private final InspectionBLL  inspectionBLL  = new InspectionBLL();
    private final PenaltyBLL     penaltyBLL     = new PenaltyBLL();
    private final PartPriceBLL   partPriceBLL   = new PartPriceBLL();
    private final CustomerDAO    customerDAO    = new CustomerDAO();
    private final VehicleDAO     vehicleDAO     = new VehicleDAO();

    // ── State ─────────────────────────────────────────────────
    private Contracts contract;
    private Customers fullCustomer;
    private Vehicles  fullVehicle;
    private Runnable  onCompleted;

    // Danh sách phụ tùng bị hư hỏng (người dùng chọn)
    private final List<PartPrices> selectedDamageParts = new ArrayList<>();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================================
    // NHẬN DỮ LIỆU
    // =========================================================
    public void setContract(Contracts contract, Runnable onCompleted) {
        this.contract    = contract;
        this.onCompleted = onCompleted;

        if (contract.getId_customer() != null)
            fullCustomer = customerDAO.findById(contract.getId_customer().getId_customer());
        if (contract.getId_vehicle() != null)
            fullVehicle = vehicleDAO.findById(contract.getId_vehicle().getId_vehicle());

        setupUI();
        fillContractInfo();
        setupDamageSection();
        recalculate();
    }

    // =========================================================
    // SETUP UI
    // =========================================================
    private void setupUI() {
        spinReturnHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, LocalDateTime.now().getHour()));
        spinReturnMin .setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, LocalDateTime.now().getMinute()));
        spinReturnHour.setEditable(true);
        spinReturnMin .setEditable(true);

        dpReturnDate.setValue(LocalDate.now());

        int fuelStart = (contract != null) ? contract.getFuel_start() : 100;
        sliderFuel.setMin(0);
        sliderFuel.setMax(100);
        sliderFuel.setValue(fuelStart);
        sliderFuel.valueProperty().addListener((obs, o, n) -> {
            if (lblFuelPercent != null) lblFuelPercent.setText((int) sliderFuel.getValue() + "%");
            recalculate();
        });
        if (lblFuelPercent != null) lblFuelPercent.setText(fuelStart + "%");

        if (txtKmEnd != null && contract != null)
            txtKmEnd.setText(String.valueOf(contract.getKm_start()));

        dpReturnDate.valueProperty().addListener((obs, o, n) -> recalculate());
        spinReturnHour.valueProperty().addListener((obs, o, n) -> recalculate());
        spinReturnMin .valueProperty().addListener((obs, o, n) -> recalculate());

        cbPaymentStatus.getItems().addAll("Chưa thanh toán", "Thanh toán 1 phần", "Đã thanh toán");
        cbPaymentStatus.setValue("Đã thanh toán");

        if (boxLateWarning != null) {
            boxLateWarning.setVisible(false);
            boxLateWarning.setManaged(false);
        }

        // Ẩn label phạt hư hỏng ban đầu
        if (lblDamagePenalty != null) {
            lblDamagePenalty.setText("0 đ");
            lblDamagePenalty.setStyle("-fx-text-fill: #64748b;");
        }
    }

    private void fillContractInfo() {
        if (contract == null) return;
        setLabel(lblContractCode, contract.getCode_contract());
        setLabel(lblCustomerName, fullCustomer != null ? fullCustomer.getFull_name() : "Không tìm thấy");
        if (fullVehicle != null) {
            setLabel(lblVehicleInfo, fullVehicle.getBrand() + " " + fullVehicle.getModel()
                    + " – " + fullVehicle.getCode_vehicle());
        }
        if (contract.getEnd_datetime() != null)
            setLabel(lblPlannedReturn, contract.getEnd_datetime().format(FMT));
    }

    // =========================================================
    // SETUP SECTION HƯ HỎNG
    // =========================================================
    private void setupDamageSection() {
        if (chkDamage == null) return;

        // Mặc định ẩn chi tiết hư hỏng
        if (boxDamageDetails != null) {
            boxDamageDetails.setVisible(false);
            boxDamageDetails.setManaged(false);
        }

        // Khi tick checkbox → hiện/ẩn danh sách phụ tùng
        chkDamage.selectedProperty().addListener((obs, old, selected) -> {
            if (boxDamageDetails != null) {
                boxDamageDetails.setVisible(selected);
                boxDamageDetails.setManaged(selected);
            }
            if (!selected) {
                // Bỏ tick → xóa hết hư hỏng đã chọn
                selectedDamageParts.clear();
                if (listDamageParts != null) listDamageParts.getItems().clear();
                recalculate();
            }
        });

        // Load danh sách phụ tùng từ bảng partprices
        loadPartPricesList();
    }

    /**
     * Load danh sách phụ tùng từ DB vào ListView để nhân viên tick chọn.
     */
    private void loadPartPricesList() {
        if (listDamageParts == null) return;

        try {
            List<PartPrices> allParts = partPriceBLL.getAllPartPrices();
            listDamageParts.getItems().clear();

            for (PartPrices part : allParts) {
                listDamageParts.getItems().add(
                        part.getPart_name() + " (" + part.getVehicle() + ") — "
                                + String.format("%,.0f đ", part.getPrice()).replace(",", "."));
            }

            // Cho phép chọn nhiều
            listDamageParts.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            // Khi chọn/bỏ chọn → cập nhật selectedDamageParts
            listDamageParts.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
                selectedDamageParts.clear();
                List<Integer> selectedIndices = listDamageParts.getSelectionModel().getSelectedIndices();
                for (int idx : selectedIndices) {
                    if (idx >= 0 && idx < allParts.size()) {
                        selectedDamageParts.add(allParts.get(idx));
                    }
                }
                updateDamageTotal();
                recalculate();
            });

        } catch (Exception e) {
            System.err.println("Lỗi load phụ tùng: " + e.getMessage());
        }
    }

    private void updateDamageTotal() {
        double total = selectedDamageParts.stream()
                .mapToDouble(PartPrices::getPrice).sum();
        if (lblDamageTotal != null)
            lblDamageTotal.setText("Tổng phạt hư hỏng: "
                    + String.format("%,.0f đ", total).replace(",", "."));
    }

    // =========================================================
    // TÍNH TIỀN
    // =========================================================
    private void recalculate() {
        if (contract == null) return;
        LocalDateTime returnDt = buildReturnDatetime();
        if (returnDt == null) return;

        // Phạt trễ
        double latePenalty = 0;
        boolean isLate = returnDt.isAfter(contract.getEnd_datetime());
        if (isLate) {
            latePenalty = calculateLatePenalty(contract.getEnd_datetime(), returnDt);
            showLateWarning(contract.getEnd_datetime(), returnDt);
        } else {
            hideLateWarning();
        }

        // Phạt xăng
        int fuelEnd  = (int) sliderFuel.getValue();
        int capacity = (fullVehicle != null) ? fullVehicle.getFuel_capacity() : 0;
        double fuelPenalty = calculateFuelPenalty(contract.getFuel_start(), fuelEnd, capacity);

        // Phạt hư hỏng
        double damagePenalty = selectedDamageParts.stream()
                .mapToDouble(PartPrices::getPrice).sum();

        double base  = contract.getBase_price() - contract.getDiscount_amount();
        double total = base + latePenalty + fuelPenalty + damagePenalty;

        setLabel(lblBasePrice,    fmt(base));
        setLabel(lblLatePenalty,  latePenalty  > 0 ? "+ " + fmt(latePenalty)  : "0 đ");
        setLabel(lblFuelPenalty,  fuelPenalty  > 0 ? "+ " + fmt(fuelPenalty)  : "0 đ");
        setLabel(lblDamagePenalty,damagePenalty > 0 ? "+ " + fmt(damagePenalty): "0 đ");
        setLabel(lblFinalTotal,   fmt(total));

        // Màu sắc
        if (lblLatePenalty != null)
            lblLatePenalty.setStyle(latePenalty > 0
                    ? "-fx-text-fill: #e11d48; -fx-font-weight: bold;" : "-fx-text-fill: #64748b;");
        if (lblFuelPenalty != null)
            lblFuelPenalty.setStyle(fuelPenalty > 0
                    ? "-fx-text-fill: #f59e0b; -fx-font-weight: bold;" : "-fx-text-fill: #64748b;");
        if (lblDamagePenalty != null)
            lblDamagePenalty.setStyle(damagePenalty > 0
                    ? "-fx-text-fill: #7c3aed; -fx-font-weight: bold;" : "-fx-text-fill: #64748b;");
    }

    // =========================================================
    // XÁC NHẬN TRẢ XE
    // =========================================================
    @FXML
    void handleConfirm() {
        LocalDateTime returnDt = buildReturnDatetime();
        if (returnDt == null) { showAlert(Alert.AlertType.WARNING, "Vui lòng chọn ngày giờ trả xe!"); return; }

        int kmEnd;
        try {
            kmEnd = Integer.parseInt(txtKmEnd.getText().trim());
            if (kmEnd < contract.getKm_start()) {
                showAlert(Alert.AlertType.WARNING, "KM trả xe không thể nhỏ hơn KM lúc giao!"); return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Số KM trả xe không hợp lệ!"); return;
        }

        try {
            // ── 1. Cập nhật hợp đồng ──
            contract.setReturn_datetime(returnDt);
            contract.setFuel_end((int) sliderFuel.getValue());
            contract.setKm_end(kmEnd);
            contract.setPayment_status(displayToPaymentStatus(cbPaymentStatus.getValue()));

            boolean ok = contractBLL.returnVehicle(contract);
            if (!ok) { showAlert(Alert.AlertType.ERROR, "Không thể cập nhật hợp đồng!"); return; }

            // ── 2. Cập nhật xe ──
            if (fullVehicle != null) {
                // Nếu có hư hỏng → xe vào bảo dưỡng, ngược lại → sẵn sàng
                if (chkDamage != null && chkDamage.isSelected() && !selectedDamageParts.isEmpty()) {
                    fullVehicle.setStatus(StatusVehicle.MAINTENANCE);
                } else {
                    fullVehicle.setStatus(StatusVehicle.AVAILABLE);
                }
                fullVehicle.setCurrent_km(kmEnd);
                vehicleDAO.update(fullVehicle);
            }

            // ── 3. Lập biên bản kiểm tra TRẢ XE ──
            Inspections inspection = new Inspections();
            inspection.setId_contract(contract);
            inspection.setId_user(AppSession.getCurrentUser());
            inspection.setInspection_type(InspectionType.TRA_XE);
            inspectionBLL.createInspection(inspection);

            // ── 4. Tạo khoản phạt hư hỏng (nếu có) ──
            if (chkDamage != null && chkDamage.isSelected()) {
                for (PartPrices part : selectedDamageParts) {
                    Penalties penalty = new Penalties();
                    penalty.setId_contract(contract);
                    penalty.setPenalty_type(PenaltyType.HU_HONG);
                    penalty.setAmount(part.getPrice());
                    penaltyBLL.createPenalty(penalty);
                }
            }

            // ── 5. Thông báo kết quả ──
            StringBuilder msg = new StringBuilder();
            msg.append("✅ Hợp đồng ").append(contract.getCode_contract()).append(" đã hoàn thành!\n");

            if (chkDamage != null && chkDamage.isSelected() && !selectedDamageParts.isEmpty()) {
                msg.append("🔧 Xe chuyển sang trạng thái Bảo dưỡng do có hư hỏng.\n");
                msg.append("Các khoản phạt đã được ghi nhận:\n");
                for (PartPrices p : selectedDamageParts) {
                    msg.append("  • ").append(p.getPart_name()).append(": ")
                            .append(String.format("%,.0f đ", p.getPrice()).replace(",", ".")).append("\n");
                }
            } else {
                msg.append("🏍 Xe đã chuyển sang trạng thái Sẵn sàng.");
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Hoàn thành hợp đồng");
            success.setHeaderText(null);
            success.setContentText(msg.toString());
            success.showAndWait();

            if (onCompleted != null) onCompleted.run();
            closeStage();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML void handleCancel() { closeStage(); }

    // =========================================================
    // HELPERS
    // =========================================================
    private LocalDateTime buildReturnDatetime() {
        if (dpReturnDate.getValue() == null) return null;
        int h = spinReturnHour.getValue() != null ? spinReturnHour.getValue() : 8;
        int m = spinReturnMin .getValue() != null ? spinReturnMin .getValue() : 0;
        return LocalDateTime.of(dpReturnDate.getValue(), LocalTime.of(h, m));
    }

    private double calculateLatePenalty(LocalDateTime expected, LocalDateTime actual) {
        if (!actual.isAfter(expected)) return 0;
        long hours = (java.time.Duration.between(expected, actual).toMinutes() + 59) / 60;
        return hours * 100_000.0;
    }

    private double calculateFuelPenalty(int fuelStart, int fuelEnd, int capacity) {
        if (fuelEnd >= fuelStart || capacity <= 0) return 0;
        return ((fuelStart - fuelEnd) * capacity / 100.0) * 25_000.0;
    }

    private void showLateWarning(LocalDateTime expected, LocalDateTime actual) {
        if (boxLateWarning == null) return;
        boxLateWarning.setVisible(true);
        boxLateWarning.setManaged(true);
        long hours = (java.time.Duration.between(expected, actual).toMinutes() + 59) / 60;
        if (lblLateMsg != null) lblLateMsg.setText("Trễ " + hours + " giờ so với " + expected.format(FMT));
    }

    private void hideLateWarning() {
        if (boxLateWarning == null) return;
        boxLateWarning.setVisible(false);
        boxLateWarning.setManaged(false);
    }

    private PaymentStatus displayToPaymentStatus(String s) {
        if (s == null) return PaymentStatus.CHUA_THANH_TOAN;
        return switch (s) {
            case "Đã thanh toán"     -> PaymentStatus.DA_THANH_TOAN;
            case "Thanh toán 1 phần" -> PaymentStatus.THANH_TOAN_1_PHAN;
            default                  -> PaymentStatus.CHUA_THANH_TOAN;
        };
    }

    private void setLabel(Label lbl, String text) { if (lbl != null) lbl.setText(text); }
    private String fmt(double v) { return String.format("%,.0f đ", v).replace(",", "."); }
    private void closeStage() { ((Stage) dpReturnDate.getScene().getWindow()).close(); }
    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type); a.setTitle("Thông báo"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}