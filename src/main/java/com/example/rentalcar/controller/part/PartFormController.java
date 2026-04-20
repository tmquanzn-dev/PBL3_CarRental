//package com.example.rentalcar.controller.part;
//
//import com.example.rentalcar.dao.PartPriceDAO;
//import com.example.rentalcar.models.PartPrices;
//import javafx.collections.FXCollections;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.scene.Node;
//import javafx.scene.control.Alert;
//import javafx.scene.control.ComboBox;
//import javafx.scene.control.Label;
//import javafx.scene.control.TextField;
//import javafx.stage.Stage;
//
//public class PartFormController {
//
//    public enum Mode { ADD, EDIT }
//
//    @FXML private Label     lblTitle;
//    @FXML private TextField txtPartName;
//    @FXML private ComboBox<String> cbVehicleType;
//    @FXML private TextField txtPrice;
//    @FXML private Label     lblPricePreview;
//    @FXML private javafx.scene.control.Button btnSave;
//
//    private final PartPriceDAO partPriceDAO = new PartPriceDAO();
//    private Mode currentMode = Mode.ADD;
//    private PartPrices editingPart;
//    private Runnable onSaved;
//
//    // Các loại xe mặc định — Admin có thể mở rộng sau
//    private static final String[] VEHICLE_TYPES = {
//            "Tất cả", "Tay ga", "Xe số", "Honda", "Yamaha", "Suzuki", "SYM", "Khác"
//    };
//
//    @FXML
//    public void initialize() {
//        cbVehicleType.setItems(FXCollections.observableArrayList(VEHICLE_TYPES));
//
//        // Hiện preview giá khi người dùng gõ
//        txtPrice.textProperty().addListener((obs, old, val) -> updatePricePreview(val));
//    }
//
//    // ==========================================================
//    // NHẬN DỮ LIỆU TỪ CONTROLLER MẸ
//    // ==========================================================
//    public void setMode(Mode mode, PartPrices part) {
//        this.currentMode = mode;
//        this.editingPart = part;
//
//        if (mode == Mode.EDIT && part != null) {
//            lblTitle.setText("Sửa phụ tùng");
//            btnSave.setText("💾  Lưu thay đổi");
//            txtPartName.setText(part.getPart_name());
//            cbVehicleType.setValue(part.getVehicle());
//            txtPrice.setText(String.valueOf((long) part.getPrice()));
//        } else {
//            lblTitle.setText("Thêm phụ tùng mới");
//            btnSave.setText("💾  Lưu phụ tùng");
//        }
//    }
//
//    public void setOnSaved(Runnable callback) {
//        this.onSaved = callback;
//    }
//
//    // ==========================================================
//    // LƯU
//    // ==========================================================
//    @FXML
//    void handleSave(ActionEvent event) {
//        // Validate
//        String name = txtPartName.getText().trim();
//        String vehicleType = cbVehicleType.getValue();
//        String priceStr = txtPrice.getText().trim();
//
//        if (name.isBlank()) {
//            showError("Vui lòng nhập tên phụ tùng!");
//            txtPartName.requestFocus();
//            return;
//        }
//        if (vehicleType == null || vehicleType.isBlank()) {
//            showError("Vui lòng chọn loại xe áp dụng!");
//            return;
//        }
//        double price;
//        try {
//            price = Double.parseDouble(priceStr.replace(",", "").replace(".", ""));
//            if (price <= 0) throw new NumberFormatException();
//        } catch (NumberFormatException e) {
//            showError("Đơn giá không hợp lệ! Vui lòng nhập số nguyên dương.");
//            txtPrice.requestFocus();
//            return;
//        }
//
//        // Lưu vào DB
//        boolean success;
//        if (currentMode == Mode.ADD) {
//            PartPrices newPart = new PartPrices(0, name, vehicleType, price);
//            success = partPriceDAO.insert(newPart);
//        } else {
//            editingPart.setPart_name(name);
//            editingPart.setVehicle(vehicleType);
//            editingPart.setPrice(price);
//            success = partPriceDAO.update(editingPart);
//        }
//
//        if (success) {
//            if (onSaved != null) onSaved.run();
//            closeStage(event);
//        } else {
//            showError("Không thể lưu. Vui lòng thử lại!");
//        }
//    }
//
//    @FXML
//    void handleClose(ActionEvent event) {
//        closeStage(event);
//    }
//
//    // ==========================================================
//    // TIỆN ÍCH
//    // ==========================================================
//    private void updatePricePreview(String val) {
//        try {
//            double price = Double.parseDouble(val.replace(",", ""));
//            lblPricePreview.setText("→ " + String.format("%,.0f đ", price).replace(",", "."));
//        } catch (NumberFormatException e) {
//            lblPricePreview.setText(val.isBlank() ? "" : "Nhập số hợp lệ");
//        }
//    }
//
//    private void closeStage(ActionEvent event) {
//        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        stage.close();
//    }
//
//    private void showError(String message) {
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.setTitle("Lỗi nhập liệu");
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//}