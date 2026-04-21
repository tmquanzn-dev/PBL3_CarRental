package com.example.rentalcar.controller.contract.booking;

import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.models.Vehicles;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Step2Controller – Bước 2: Chọn xe thuê.
 *
 * Chức năng:
 *  1. Load tất cả xe có status = AVAILABLE từ VehicleBLL.
 *  2. Hiển thị dưới dạng card trong FlowPane.
 *  3. Lọc theo hãng xe và loại xe.
 *  4. Khi click card → highlight xe được chọn.
 *  5. validateAndSave() → lưu Vehicles vào ContractDraft.
 *
 * Đường dẫn: src/main/java/com/example/rentalcar/controller/contract/booking/Step2Controller.java
 */
public class Step2Controller {

    // =========================================================
    //  FXML
    // =========================================================
    @FXML private FlowPane  flowPaneVehicles;
    @FXML private ComboBox<String> cbBrand;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField txtSearch;
    @FXML private Label     lblSelectedVehicle; // Hiển thị xe đang chọn

    // =========================================================
    //  STATE
    // =========================================================
    private ContractDraft draft;
    private Vehicles      selectedVehicle;
    private List<Vehicles> allAvailable;
    private VBox          selectedCard; // Card đang được highlight

    private final VehicleBLL vehicleBLL = new VehicleBLL();
    private final NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));

    // =========================================================
    //  NHẬN DRAFT
    // =========================================================
    public void setDraft(ContractDraft draft) {
        this.draft = draft;

        // Nếu đã chọn xe trước đó (quay lại từ bước 3)
        if (draft.getSelectedVehicle() != null) {
            selectedVehicle = draft.getSelectedVehicle();
        }

        loadAvailableVehicles();
        setupFilters();
    }

    // =========================================================
    //  LOAD XE
    // =========================================================
    private void loadAvailableVehicles() {
        allAvailable = vehicleBLL.getAllVehicles().stream()
                .filter(v -> v.getStatus() == StatusVehicle.AVAILABLE)
                .collect(Collectors.toList());

        renderCards(allAvailable);
    }

    /** Render danh sách xe thành card trong FlowPane */
    private void renderCards(List<Vehicles> list) {
        flowPaneVehicles.getChildren().clear();

        if (list.isEmpty()) {
            Label empty = new Label("😔  Không có xe nào đang sẵn sàng cho thuê.");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
            flowPaneVehicles.getChildren().add(empty);
            return;
        }

        for (Vehicles v : list) {
            VBox card = buildVehicleCard(v);
            flowPaneVehicles.getChildren().add(card);

            // Highlight lại xe đã chọn trước (khi quay lại bước 2)
            if (selectedVehicle != null && selectedVehicle.getId_vehicle() == v.getId_vehicle()) {
                applySelectedStyle(card);
                selectedCard = card;
                updateSelectedLabel(v);
            }
        }
    }

    /** Tạo 1 card xe */
    private VBox buildVehicleCard(Vehicles v) {
        VBox card = new VBox(8);
        card.setPrefWidth(220);
        card.setPrefHeight(260);
        card.setPadding(new Insets(14));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 14;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 3);" +
                        "-fx-cursor: hand;"
        );

        // Ảnh xe
        ImageView img = new ImageView();
        img.setFitWidth(192);
        img.setFitHeight(110);
        img.setPreserveRatio(true);
        try {
            String path = (v.getImage_url() != null && !v.getImage_url().isBlank())
                    ? v.getImage_url()
                    : "/image/dashboardform/card-moto.png";
            img.setImage(new Image(getClass().getResourceAsStream(path)));
        } catch (Exception ignored) {}

        // Biển số + badge trạng thái
        Label lblPlate = new Label(v.getCode_vehicle());
        lblPlate.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #475569;");

        Label lblBadge = new Label("Sẵn sàng");
        lblBadge.setStyle(
                "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;" +
                        "-fx-padding: 3 8; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;"
        );

        HBox headerRow = new HBox(8, lblPlate, lblBadge);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Tên xe
        Label lblName = new Label(v.getBrand() + " " + v.getModel());
        lblName.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b; -fx-font-weight: bold;");
        lblName.setWrapText(true);

        // Giá
        Label lblPrice = new Label(fmt.format((long) v.getPrice_day()) + " đ/ngày");
        lblPrice.setStyle("-fx-font-size: 13px; -fx-text-fill: #10b981; -fx-font-weight: bold;");

        // Loại xe
        Label lblType = new Label(v.getVehicle_type() != null ? v.getVehicle_type() : "");
        lblType.setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;" +
                        "-fx-padding: 2 8; -fx-background-radius: 6; -fx-font-size: 11px;"
        );

        card.getChildren().addAll(img, headerRow, lblName, lblPrice, lblType);

        // Sự kiện click chọn xe
        card.setOnMouseClicked(e -> {
            // Bỏ highlight card cũ
            if (selectedCard != null) removeSelectedStyle(selectedCard);

            // Highlight card mới
            applySelectedStyle(card);
            selectedCard  = card;
            selectedVehicle = v;
            updateSelectedLabel(v);
        });

        // Hover effect
        card.setOnMouseEntered(e -> {
            if (card != selectedCard)
                card.setStyle(card.getStyle().replace("#e2e8f0", "#146dff"));
        });
        card.setOnMouseExited(e -> {
            if (card != selectedCard)
                card.setStyle(card.getStyle().replace("#146dff", "#e2e8f0"));
        });

        return card;
    }

    private void applySelectedStyle(VBox card) {
        card.setStyle(card.getStyle()
                .replace("-fx-border-color: #e2e8f0;", "-fx-border-color: #146dff; -fx-border-width: 2;")
                .replace("-fx-background-color: white;", "-fx-background-color: #eff6ff;")
        );
    }

    private void removeSelectedStyle(VBox card) {
        card.setStyle(card.getStyle()
                .replace("-fx-border-color: #146dff; -fx-border-width: 2;", "-fx-border-color: #e2e8f0;")
                .replace("-fx-background-color: #eff6ff;", "-fx-background-color: white;")
        );
    }

    private void updateSelectedLabel(Vehicles v) {
        if (lblSelectedVehicle != null) {
            lblSelectedVehicle.setText("✅  Đang chọn: " + v.getBrand() + " " + v.getModel()
                    + " – " + v.getCode_vehicle());
            lblSelectedVehicle.setStyle("-fx-text-fill: #146dff; -fx-font-weight: bold; -fx-font-size: 13px;");
        }
    }

    // =========================================================
    //  FILTER
    // =========================================================
    private void setupFilters() {
        if (cbBrand == null || cbType == null || allAvailable == null) return;

        cbBrand.getItems().clear();
        cbBrand.getItems().add("Tất cả");
        allAvailable.stream()
                .map(Vehicles::getBrand)
                .distinct().sorted()
                .forEach(cbBrand.getItems()::add);
        cbBrand.setValue("Tất cả");

        cbType.getItems().clear();
        cbType.getItems().add("Tất cả");
        allAvailable.stream()
                .map(Vehicles::getVehicle_type)
                .filter(t -> t != null && !t.isBlank())
                .distinct().sorted()
                .forEach(cbType.getItems()::add);
        cbType.setValue("Tất cả");
    }

    @FXML
    void handleFilter() {
        if (allAvailable == null) return;

        String brand   = cbBrand  != null ? cbBrand.getValue()  : "Tất cả";
        String type    = cbType   != null ? cbType.getValue()   : "Tất cả";
        String keyword = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";

        List<Vehicles> filtered = allAvailable.stream()
                .filter(v -> brand.equals("Tất cả") || v.getBrand().equalsIgnoreCase(brand))
                .filter(v -> type.equals("Tất cả")  ||
                        (v.getVehicle_type() != null && v.getVehicle_type().equalsIgnoreCase(type)))
                .filter(v -> keyword.isEmpty()
                        || v.getCode_vehicle().toLowerCase().contains(keyword)
                        || (v.getModel() != null && v.getModel().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());

        renderCards(filtered);
    }

    // =========================================================
    //  VALIDATE & LƯU VÀO DRAFT
    // =========================================================
    public boolean validateAndSave() {
        if (selectedVehicle == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Chưa chọn xe");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn một xe trước khi tiếp tục!");
            alert.showAndWait();
            return false;
        }

        draft.setSelectedVehicle(selectedVehicle);
        // Lưu KM hiện tại của xe vào draft
        draft.setKmStart(selectedVehicle.getCurrent_km());
        return true;
    }
}