package com.example.rentalcar.controller.partprice;

import com.example.rentalcar.bll.PartPriceBLL;
import com.example.rentalcar.models.PartPrices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PartPriceController implements Initializable {

    @FXML private Label lblTotalParts;
    @FXML private Label lblAvgPrice;
    @FXML private Label lblVehicleTypes;
    @FXML private Label lblCount;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbVehicleType;

    @FXML private TableView<PartPrices> tablePartPrices;
    @FXML private TableColumn<PartPrices, Integer> colId;
    @FXML private TableColumn<PartPrices, String>  colName;
    @FXML private TableColumn<PartPrices, String>  colType;
    @FXML private TableColumn<PartPrices, Double>  colPrice;
    @FXML private TableColumn<PartPrices, Void>    colAction;

    private final PartPriceBLL bll = new PartPriceBLL();
    private ObservableList<PartPrices> masterList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadData();
    }

    // ============================================================
    // SETUP BẢNG
    // ============================================================
    private void setupTableColumns() {
        // STT tự động
        colId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
                setStyle("-fx-alignment: CENTER; -fx-text-fill: #64748b; -fx-font-size: 13px;");
            }
        });

        // Tên phụ tùng
        colName.setCellValueFactory(new PropertyValueFactory<>("part_name"));
        colName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) { setGraphic(null); return; }
                Label lbl = new Label(name);
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");
                setGraphic(lbl);
            }
        });

        // Loại xe - badge tím
        colType.setCellValueFactory(new PropertyValueFactory<>("vehicle"));
        colType.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) { setGraphic(null); return; }
                Label badge = new Label(type);
                badge.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca;" +
                        " -fx-padding: 4 12; -fx-background-radius: 8;" +
                        " -fx-font-weight: bold; -fx-font-size: 12px;");
                HBox box = new HBox(badge);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        // Giá tiền - đỏ đậm
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) { setGraphic(null); return; }
                Label lbl = new Label(String.format("%,.0f đ", price).replace(",", "."));
                lbl.setStyle("-fx-text-fill: #e11d48; -fx-font-weight: bold; -fx-font-size: 14px;");
                setGraphic(lbl);
            }
        });

        // Thao tác - Edit + Delete
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button();
            private final Button btnDelete = new Button();
            private final HBox   pane      = new HBox(10, btnEdit, btnDelete);

            {
                pane.setAlignment(Pos.CENTER_LEFT);
                btnEdit.getStyleClass().add("btn-action");
                btnDelete.getStyleClass().add("btn-action");
                loadIcon(btnEdit, "/icon/dashboardform/edit.png");
                loadIcon(btnDelete, "/icon/dashboardform/delete.png");

                btnEdit.setOnAction(e -> {
                    PartPrices part = getTableView().getItems().get(getIndex());
                    openForm(part); // EDIT mode
                });
                btnDelete.setOnAction(e -> {
                    PartPrices part = getTableView().getItems().get(getIndex());
                    confirmDelete(part);
                });
            }

            private void loadIcon(Button btn, String path) {
                try {
                    URL url = getClass().getResource(path);
                    if (url != null) {
                        ImageView iv = new ImageView(new Image(url.toExternalForm()));
                        iv.setFitHeight(18); iv.setFitWidth(18);
                        btn.setGraphic(iv);
                    }
                } catch (Exception ignored) {}
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    // ============================================================
    // LOAD DỮ LIỆU
    // ============================================================
    private void loadData() {
        try {
            List<PartPrices> list = bll.getAllPartPrices();
            masterList = FXCollections.observableArrayList(list);
            tablePartPrices.setItems(masterList);
            tablePartPrices.setFixedCellSize(60.0);

            updateStats(list);
            populateVehicleFilter(list);
            if (lblCount != null) lblCount.setText(list.size() + " phụ tùng");
        } catch (Exception e) {
            showAlert("Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void updateStats(List<PartPrices> list) {
        if (lblTotalParts != null) lblTotalParts.setText(String.valueOf(list.size()));

        if (lblAvgPrice != null) {
            double avg = list.stream().mapToDouble(PartPrices::getPrice).average().orElse(0);
            lblAvgPrice.setText(String.format("%,.0f đ", avg).replace(",", "."));
        }

        if (lblVehicleTypes != null) {
            long types = list.stream()
                    .map(PartPrices::getVehicle)
                    .filter(v -> v != null && !v.isBlank())
                    .distinct().count();
            lblVehicleTypes.setText(String.valueOf(types));
        }
    }

    private void populateVehicleFilter(List<PartPrices> list) {
        if (cbVehicleType == null) return;
        List<String> types = list.stream()
                .map(PartPrices::getVehicle)
                .filter(v -> v != null && !v.isBlank())
                .distinct().sorted().collect(Collectors.toList());
        types.add(0, "Tất cả");
        cbVehicleType.setItems(FXCollections.observableArrayList(types));
        cbVehicleType.getSelectionModel().selectFirst();
    }

    // ============================================================
    // TÌM KIẾM & LỌC
    // ============================================================
    @FXML void handleSearch(ActionEvent event)  { applyFilter(); }
    @FXML void handleFilter(ActionEvent event)  { applyFilter(); }
    @FXML void handleSearchLive()               { applyFilter(); }

    private void applyFilter() {
        if (masterList == null) return;
        String keyword = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        String type    = cbVehicleType != null ? cbVehicleType.getValue() : "Tất cả";

        ObservableList<PartPrices> filtered = masterList.filtered(p -> {
            boolean matchKey  = keyword.isEmpty() ||
                    (p.getPart_name() != null && p.getPart_name().toLowerCase().contains(keyword));
            boolean matchType = type == null || type.equals("Tất cả") ||
                    type.equals(p.getVehicle());
            return matchKey && matchType;
        });

        tablePartPrices.setItems(filtered);
        if (lblCount != null) lblCount.setText(filtered.size() + " phụ tùng");
    }

    @FXML void handleReload(ActionEvent event) {
        if (txtSearch != null)    txtSearch.clear();
        if (cbVehicleType != null) cbVehicleType.getSelectionModel().selectFirst();
        loadData();
    }

    // ============================================================
    // MỞ FORM THÊM MỚI
    // ============================================================
    @FXML void handleAddNewPart(ActionEvent event) {
        openForm(null); // ADD mode
    }

    // ============================================================
    // FORM THÊM / SỬA (dùng chung)
    // ============================================================
    private void openForm(PartPrices part) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/partprice/PartFormModal.fxml"));
            Parent root = loader.load();

            PartFormController ctrl = loader.getController();
            ctrl.setMode(part == null ? PartFormController.Mode.ADD : PartFormController.Mode.EDIT, part);
            ctrl.setOnSaved(this::loadData); // callback reload sau khi lưu

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(part == null ? "Thêm phụ tùng mới" : "Sửa: " + part.getPart_name());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (Exception e) {
            showAlert("Lỗi mở form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================
    // XÁC NHẬN XÓA
    // ============================================================
    private void confirmDelete(PartPrices part) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa phụ tùng:\n\"" + part.getPart_name() + "\"?");
        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            if (bll.deletePartPrice(part.getId_part_price())) {
                loadData();
            } else {
                showAlert("Không thể xóa phụ tùng này!");
            }
        });
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Lỗi"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}