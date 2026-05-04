package com.example.rentalcar.controller.contract;

import com.example.rentalcar.bll.ContractBLL;
import com.example.rentalcar.dao.CustomerDAO;
import com.example.rentalcar.dao.VehicleDAO;
import com.example.rentalcar.models.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ReturnVehicleController {

    @FXML private Label lblContractCode;
    @FXML private Label lblCustomerName;
    @FXML private Label lblVehicleInfo;
    @FXML private Label lblPlannedReturn;

    @FXML private DatePicker dpReturnDate;
    @FXML private Spinner<Integer> spinReturnHour;
    @FXML private Spinner<Integer> spinReturnMin;

    @FXML private Slider sliderFuel;
    @FXML private Label lblFuelPercent;
    @FXML private TextField txtKmEnd;

    @FXML private Label lblBasePrice;
    @FXML private Label lblLatePenalty;
    @FXML private Label lblFuelPenalty;
    @FXML private Label lblFinalTotal;

    @FXML private HBox boxLateWarning;
    @FXML private Label lblLateMsg;

    @FXML private ComboBox<String> cbPaymentStatus;

    private Contracts contract;
    private Customers fullCustomer;
    private Vehicles  fullVehicle;
    private Runnable  onCompleted;

    private final ContractBLL contractBLL = new ContractBLL();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final VehicleDAO  vehicleDAO  = new VehicleDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setContract(Contracts contract, Runnable onCompleted) {
        this.contract    = contract;
        this.onCompleted = onCompleted;

        // Load đầy đủ từ DB — tránh shell object chỉ có ID
        if (contract.getId_customer() != null)
            fullCustomer = customerDAO.findById(contract.getId_customer().getId_customer());
        if (contract.getId_vehicle() != null)
            fullVehicle = vehicleDAO.findById(contract.getId_vehicle().getId_vehicle());

        setupUI();
        fillContractInfo();
        recalculate();
    }

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
    }

    private void fillContractInfo() {
        if (contract == null) return;

        setLabel(lblContractCode, contract.getCode_contract());

        // Dùng fullCustomer load từ DB — không còn null
        setLabel(lblCustomerName,
                fullCustomer != null ? fullCustomer.getFull_name() : "Không tìm thấy");

        // Dùng fullVehicle load từ DB
        if (fullVehicle != null) {
            setLabel(lblVehicleInfo,
                    fullVehicle.getBrand() + " " + fullVehicle.getModel()
                            + " – " + fullVehicle.getCode_vehicle());
        } else {
            setLabel(lblVehicleInfo, "Không tìm thấy");
        }

        if (contract.getEnd_datetime() != null)
            setLabel(lblPlannedReturn, contract.getEnd_datetime().format(FMT));
    }

    private void recalculate() {
        if (contract == null) return;
        LocalDateTime returnDt = buildReturnDatetime();
        if (returnDt == null) return;

        double latePenalty = 0;
        boolean isLate = returnDt.isAfter(contract.getEnd_datetime());
        if (isLate) {
            latePenalty = calculateLatePenalty(contract.getEnd_datetime(), returnDt);
            showLateWarning(contract.getEnd_datetime(), returnDt);
        } else {
            hideLateWarning();
        }

        int fuelEnd  = (int) sliderFuel.getValue();
        int capacity = (fullVehicle != null) ? fullVehicle.getFuel_capacity() : 0;
        double fuelPenalty = calculateFuelPenalty(contract.getFuel_start(), fuelEnd, capacity);

        double base  = contract.getBase_price() - contract.getDiscount_amount();
        double total = base + latePenalty + fuelPenalty;

        setLabel(lblBasePrice,   fmt(base));
        setLabel(lblLatePenalty, latePenalty > 0 ? "+ " + fmt(latePenalty) : "0 đ");
        setLabel(lblFuelPenalty, fuelPenalty > 0 ? "+ " + fmt(fuelPenalty) : "0 đ");
        setLabel(lblFinalTotal,  fmt(total));

        if (lblLatePenalty != null)
            lblLatePenalty.setStyle(latePenalty > 0
                    ? "-fx-text-fill: #e11d48; -fx-font-weight: bold;" : "-fx-text-fill: #64748b;");
        if (lblFuelPenalty != null)
            lblFuelPenalty.setStyle(fuelPenalty > 0
                    ? "-fx-text-fill: #f59e0b; -fx-font-weight: bold;" : "-fx-text-fill: #64748b;");
    }

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
            contract.setReturn_datetime(returnDt);
            contract.setFuel_end((int) sliderFuel.getValue());
            contract.setKm_end(kmEnd);
            contract.setPayment_status(displayToPaymentStatus(cbPaymentStatus.getValue()));

            boolean ok = contractBLL.returnVehicle(contract);
            if (!ok) { showAlert(Alert.AlertType.ERROR, "Không thể cập nhật hợp đồng!"); return; }

            if (fullVehicle != null) {
                fullVehicle.setStatus(StatusVehicle.AVAILABLE);
                fullVehicle.setCurrent_km(kmEnd);
                vehicleDAO.update(fullVehicle);
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Hoàn thành hợp đồng");
            success.setHeaderText(null);
            success.setContentText("✅  Hợp đồng " + contract.getCode_contract()
                    + " đã hoàn thành!\nXe đã chuyển sang trạng thái Sẵn sàng.");
            success.showAndWait();

            if (onCompleted != null) onCompleted.run();
            closeStage();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML void handleCancel() { closeStage(); }

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