package com.example.rentalcar.controller.partprice;

import com.example.rentalcar.bll.PartPriceBLL;
import com.example.rentalcar.dao.PartPriceDAO;
import com.example.rentalcar.models.PartPrices;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class PartFormController implements Initializable {

    public enum Mode { ADD, EDIT }

    @FXML private Label     lblTitle;
    @FXML private Label     lblSubtitle;
    @FXML private TextField txtPartName;
    @FXML private ComboBox<String> cbVehicleType;
    @FXML private TextField txtPrice;
    @FXML private Label     lblPricePreview;
    @FXML private Label     lblMsg;
    @FXML private Button    btnSave;

    private final PartPriceBLL bll = new PartPriceBLL();
    private Mode       currentMode  = Mode.ADD;
    private PartPrices editingPart;
    private Runnable   onSaved;

    private static final String[] VEHICLE_TYPES = {
            "Tất cả", "Tay ga", "Xe số", "Xe côn tay",
            "Honda", "Yamaha", "Suzuki", "SYM", "Khác"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbVehicleType.setItems(FXCollections.observableArrayList(VEHICLE_TYPES));
        cbVehicleType.getSelectionModel().selectFirst();

        // Live preview giá khi gõ
        txtPrice.textProperty().addListener((obs, old, val) -> updatePreview(val));
    }

    // ============================================================
    // NHẬN DỮ LIỆU TỪ CONTROLLER MẸ
    // ============================================================
    public void setMode(Mode mode, PartPrices part) {
        this.currentMode  = mode;
        this.editingPart  = part;

        if (mode == Mode.EDIT && part != null) {
            lblTitle.setText("Chỉnh sửa phụ tùng");
            if (lblSubtitle != null)
                lblSubtitle.setText("Cập nhật thông tin: " + part.getPart_name());
            btnSave.setText("💾  Lưu thay đổi");

            txtPartName.setText(part.getPart_name());
            cbVehicleType.setValue(part.getVehicle());
            txtPrice.setText(String.valueOf((long) part.getPrice()));
        } else {
            lblTitle.setText("Thêm phụ tùng mới");
            if (lblSubtitle != null)
                lblSubtitle.setText("Nhập thông tin phụ tùng bên dưới");
            btnSave.setText("💾  Lưu phụ tùng");
        }
    }

    public void setOnSaved(Runnable callback) {
        this.onSaved = callback;
    }

    // ============================================================
    // LƯU
    // ============================================================
    @FXML void handleSave() {
        // --- Validate ---
        String name = txtPartName.getText().trim();
        if (name.isBlank()) {
            showMsg("❌  Vui lòng nhập tên phụ tùng!", false);
            txtPartName.requestFocus();
            return;
        }
        String vehicleType = cbVehicleType.getValue();
        if (vehicleType == null || vehicleType.isBlank()) {
            showMsg("❌  Vui lòng chọn loại xe áp dụng!", false);
            return;
        }
        double price;
        try {
            price = Double.parseDouble(txtPrice.getText().trim().replace(",", "").replace(".", ""));
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showMsg("❌  Đơn giá không hợp lệ! Nhập số nguyên dương.", false);
            txtPrice.requestFocus();
            return;
        }

        // --- Lưu ---
        try {
            boolean ok;
            if (currentMode == Mode.ADD) {
                PartPrices newPart = new PartPrices(0, name, vehicleType, price);
                ok = bll.addPartPrice(newPart); // Dùng BLL
            } else {
                editingPart.setPart_name(name);
                editingPart.setVehicle(vehicleType);
                editingPart.setPrice(price);
                ok = bll.updatePartPrice(editingPart); // Dùng BLL
            }

            if (ok) {
                showMsg("✅ " + (currentMode == Mode.ADD ? "Thêm phụ tùng thành công!" : "Cập nhật thành công!"), true);
                if (onSaved != null) onSaved.run();
                autoClose();
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            // BẮT LỖI BẢO MẬT TỪ BLL VÀ IN LÊN MÀN HÌNH
            showMsg("❌ " + ex.getMessage(), false);
        }
    }

    @FXML void handleClose() { getStage().close(); }

    // ============================================================
    // HELPERS
    // ============================================================
    private void updatePreview(String val) {
        try {
            double price = Double.parseDouble(val.replace(",", "").replace(".", ""));
            lblPricePreview.setText("→ " + String.format("%,.0f đ", price).replace(",", "."));
            lblPricePreview.setStyle("-fx-text-fill: #146dff; -fx-font-weight: bold;");
        } catch (NumberFormatException e) {
            lblPricePreview.setText(val.isBlank() ? "" : "⚠ Nhập số hợp lệ");
            lblPricePreview.setStyle("-fx-text-fill: #e11d48;");
        }
    }

    private void showMsg(String text, boolean success) {
        lblMsg.setText(text);
        lblMsg.setStyle(success
                ? "-fx-text-fill: #16a34a; -fx-font-size: 13px; -fx-font-weight: bold;"
                : "-fx-text-fill: #e11d48; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblMsg.setVisible(true);
        new Timeline(new KeyFrame(Duration.seconds(3),
                e -> lblMsg.setVisible(false))).play();
    }

    private void autoClose() {
        new Timeline(new KeyFrame(Duration.seconds(1.2),
                e -> getStage().close())).play();
    }

    private Stage getStage() {
        return (Stage) btnSave.getScene().getWindow();
    }
}