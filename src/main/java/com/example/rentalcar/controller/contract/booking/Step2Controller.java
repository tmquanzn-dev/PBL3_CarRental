package com.example.rentalcar.controller.contract.booking;

import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.models.Vehicles;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Step2Controller – Bước 2: Chọn xe thuê.
 * FIX: load ảnh xe đúng cách (classpath vs filesystem path)
 */
public class Step2Controller {

    @FXML private FlowPane         flowPaneVehicles;
    @FXML private ComboBox<String> cbBrand;
    @FXML private ComboBox<String> cbType;
    @FXML private TextField        txtSearch;
    @FXML private Label            lblSelectedVehicle;

    private ContractDraft  draft;
    private Vehicles       selectedVehicle;
    private List<Vehicles> allAvailable;
    private VBox           selectedCard;

    private final VehicleBLL  vehicleBLL = new VehicleBLL();
    private final NumberFormat fmt        = NumberFormat.getInstance(new Locale("vi", "VN"));

    // =========================================================
    //  NHẬN DRAFT
    // =========================================================
    public void setDraft(ContractDraft draft) {
        this.draft = draft;
        if (draft.getSelectedVehicle() != null) {
            selectedVehicle = draft.getSelectedVehicle();
        }
        loadAvailableVehicles();
        setupFilters();
    }

    // =========================================================
    //  LOAD XE AVAILABLE
    // =========================================================
    private void loadAvailableVehicles() {
        allAvailable = vehicleBLL.getAllVehicles().stream()
                .filter(v -> v.getStatus() == StatusVehicle.AVAILABLE)
                .collect(Collectors.toList());
        renderCards(allAvailable);
    }

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

            // Giữ highlight nếu quay lại từ bước 3
            if (selectedVehicle != null
                    && selectedVehicle.getId_vehicle() == v.getId_vehicle()) {
                applySelectedStyle(card);
                selectedCard = card;
                updateSelectedLabel(v);
            }
        }
    }

    // =========================================================
    //  BUILD CARD
    // =========================================================
    private VBox buildVehicleCard(Vehicles v) {
        VBox card = new VBox(8);
        card.setPrefWidth(220);
        card.setPrefHeight(290);
        card.setPadding(new Insets(14));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 14;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 3);" +
                        "-fx-cursor: hand;"
        );

        // ── Ảnh xe (FIX Bug 1) ──────────────────────────────
        ImageView img = new ImageView();
        img.setFitWidth(192);
        img.setFitHeight(110);
        img.setPreserveRatio(true);
        loadVehicleImage(img, v);

        // Biển số + badge
        Label lblPlate = new Label(v.getCode_vehicle());
        lblPlate.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #475569;");

        Label lblBadge = new Label("Sẵn sàng");
        lblBadge.setStyle(
                "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;" +
                        "-fx-padding: 3 8; -fx-background-radius: 6;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;"
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
        HBox infoRow = new HBox(8);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        if (v.getVehicle_type() != null && !v.getVehicle_type().isBlank()) {
            Label lblType = new Label(v.getVehicle_type());
            lblType.setStyle(
                    "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;" +
                            "-fx-padding: 2 8; -fx-background-radius: 6; -fx-font-size: 11px;"
            );
            infoRow.getChildren().add(lblType);
        }

        if (v.getYear_of_manufacture() > 0) {
            Label lblYear = new Label("Năm " + v.getYear_of_manufacture());
            lblYear.setStyle(
                    "-fx-background-color: #eff6ff; -fx-text-fill: #2563eb;" +
                            "-fx-padding: 2 8; -fx-background-radius: 6; -fx-font-size: 11px;"
            );
            infoRow.getChildren().add(lblYear);
        }

        // KM hiện tại
        Label lblKm = new Label("📍 " + v.getCurrent_km() + " km");
        lblKm.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        card.getChildren().addAll(img, headerRow, lblName, lblPrice, infoRow, lblKm);

        // Click
        card.setOnMouseClicked(e -> {
            if (selectedCard != null) removeSelectedStyle(selectedCard);
            applySelectedStyle(card);
            selectedCard    = card;
            selectedVehicle = v;
            updateSelectedLabel(v);
        });

        // Hover
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

    /**
     * FIX BUG 1: Load ảnh xe đúng cách.
     * DB lưu 2 kiểu:
     *   /image/dashboardform/card-moto.png  → classpath (trong jar/resources)
     *   uploads/vehicles/xxx.jpg            → file hệ thống (user.home/VehicleRent/)
     */
    private void loadVehicleImage(ImageView img, Vehicles v) {
        String imgUrl = (v.getImage_url() != null && !v.getImage_url().isBlank())
                ? v.getImage_url().trim()
                : "/image/dashboardform/card-moto.png";

        try {
            if (imgUrl.startsWith("/")) {
                // Classpath resource
                var url = getClass().getResource(imgUrl);
                if (url != null) {
                    img.setImage(new Image(url.toExternalForm(), true));
                } else {
                    loadDefaultImage(img);
                }
            } else {
                // File hệ thống (ảnh upload)
                File f = new File(imgUrl);
                if (!f.isAbsolute()) {
                    f = new File(System.getProperty("user.home")
                            + File.separator + "VehicleRent"
                            + File.separator + imgUrl);
                }
                if (f.exists()) {
                    img.setImage(new Image(f.toURI().toString(), true));
                } else {
                    loadDefaultImage(img);
                }
            }
        } catch (Exception e) {
            loadDefaultImage(img);
        }
    }

    private void loadDefaultImage(ImageView img) {
        try {
            var url = getClass().getResource("/image/dashboardform/card-moto.png");
            if (url != null) img.setImage(new Image(url.toExternalForm()));
        } catch (Exception ignored) {}
    }

    // =========================================================
    //  STYLE HELPERS
    // =========================================================
    private void applySelectedStyle(VBox card) {
        card.setStyle(card.getStyle()
                .replace("-fx-border-color: #e2e8f0;",
                        "-fx-border-color: #146dff; -fx-border-width: 2;")
                .replace("-fx-background-color: white;",
                        "-fx-background-color: #eff6ff;")
        );
    }

    private void removeSelectedStyle(VBox card) {
        card.setStyle(card.getStyle()
                .replace("-fx-border-color: #146dff; -fx-border-width: 2;",
                        "-fx-border-color: #e2e8f0;")
                .replace("-fx-background-color: #eff6ff;",
                        "-fx-background-color: white;")
        );
    }

    private void updateSelectedLabel(Vehicles v) {
        if (lblSelectedVehicle != null) {
            lblSelectedVehicle.setText("✅  Đang chọn: "
                    + v.getBrand() + " " + v.getModel()
                    + " – " + v.getCode_vehicle());
            lblSelectedVehicle.setStyle(
                    "-fx-text-fill: #146dff; -fx-font-weight: bold; -fx-font-size: 13px;");
        }
    }

    // =========================================================
    //  FILTER
    // =========================================================
    private void setupFilters() {
        if (cbBrand == null || cbType == null || allAvailable == null) return;

        cbBrand.getItems().clear();
        cbBrand.getItems().add("Tất cả");
        allAvailable.stream().map(Vehicles::getBrand)
                .distinct().sorted().forEach(cbBrand.getItems()::add);
        cbBrand.setValue("Tất cả");

        cbType.getItems().clear();
        cbType.getItems().add("Tất cả");
        allAvailable.stream().map(Vehicles::getVehicle_type)
                .filter(t -> t != null && !t.isBlank())
                .distinct().sorted().forEach(cbType.getItems()::add);
        cbType.setValue("Tất cả");
    }

    @FXML
    void handleFilter() {
        if (allAvailable == null) return;

        String brand   = cbBrand   != null ? cbBrand.getValue()              : "Tất cả";
        String type    = cbType    != null ? cbType.getValue()               : "Tất cả";
        String keyword = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";

        List<Vehicles> filtered = allAvailable.stream()
                .filter(v -> brand.equals("Tất cả") || v.getBrand().equalsIgnoreCase(brand))
                .filter(v -> type.equals("Tất cả") ||
                        (v.getVehicle_type() != null
                                && v.getVehicle_type().equalsIgnoreCase(type)))
                .filter(v -> keyword.isEmpty()
                        || v.getCode_vehicle().toLowerCase().contains(keyword)
                        || (v.getModel() != null
                        && v.getModel().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());

        renderCards(filtered);
    }

    // =========================================================
    //  VALIDATE & LƯU
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
        draft.setKmStart(selectedVehicle.getCurrent_km());
        return true;
    }
}