//package com.example.rentalcar.controller.part;
//
//import com.example.rentalcar.dao.PartPriceDAO;
//import com.example.rentalcar.models.PartPrices;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.fxml.Initializable;
//import javafx.geometry.Pos;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.HBox;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//
//import java.net.URL;
//import java.util.List;
//import java.util.ResourceBundle;
//import java.util.stream.Collectors;
//
//public class PartController implements Initializable {
//
//    @FXML private Label lblTotalParts;
//    @FXML private Label lblAvgPrice;
//    @FXML private Label lblVehicleTypes;
//    @FXML private Label lblCount;
//
//    @FXML private TextField txtSearch;
//    @FXML private ComboBox<String> cbVehicleType;
//
//    @FXML private TableView<PartPrices> tableParts;
//    @FXML private TableColumn<PartPrices, Integer> colId;
//    @FXML private TableColumn<PartPrices, String>  colName;
//    @FXML private TableColumn<PartPrices, String>  colVehicle;
//    @FXML private TableColumn<PartPrices, Double>  colPrice;
//    @FXML private TableColumn<PartPrices, Void>    colAction;
//
//    private final PartPriceDAO partPriceDAO = new PartPriceDAO();
//    private ObservableList<PartPrices> partList;
//
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//        setupTableColumns();
//        loadData();
//    }
//
//    // ==========================================================
//    // SETUP CỘT BẢNG
//    // ==========================================================
//    private void setupTableColumns() {
//        // STT tự động theo index
//        colId.setCellFactory(col -> new TableCell<>() {
//            @Override
//            protected void updateItem(Integer item, boolean empty) {
//                super.updateItem(item, empty);
//                setText(empty ? null : String.valueOf(getIndex() + 1));
//            }
//        });
//
//        colName.setCellValueFactory(new PropertyValueFactory<>("part_name"));
//        colName.setCellFactory(col -> new TableCell<>() {
//            @Override
//            protected void updateItem(String name, boolean empty) {
//                super.updateItem(name, empty);
//                if (empty || name == null) { setGraphic(null); return; }
//                Label lbl = new Label(name);
//                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
//                setGraphic(lbl);
//            }
//        });
//
//        // Cột loại xe: Badge màu tím
//        colVehicle.setCellValueFactory(new PropertyValueFactory<>("vehicle"));
//        colVehicle.setCellFactory(col -> new TableCell<>() {
//            @Override
//            protected void updateItem(String type, boolean empty) {
//                super.updateItem(type, empty);
//                if (empty || type == null) { setGraphic(null); return; }
//                Label badge = new Label(type);
//                badge.getStyleClass().add("badge-vehicle-type");
//                HBox box = new HBox(badge);
//                box.setAlignment(Pos.CENTER_LEFT);
//                setGraphic(box);
//            }
//        });
//
//        // Cột giá: Format tiền VNĐ
//        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
//        colPrice.setCellFactory(col -> new TableCell<>() {
//            @Override
//            protected void updateItem(Double price, boolean empty) {
//                super.updateItem(price, empty);
//                if (empty || price == null) { setGraphic(null); return; }
//                Label lbl = new Label(String.format("%,.0f đ", price).replace(",", "."));
//                lbl.setStyle("-fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-font-size: 14px;");
//                setGraphic(lbl);
//            }
//        });
//
//        // Cột thao tác
//        colAction.setCellFactory(col -> new TableCell<>() {
//            private final Button btnEdit   = new Button();
//            private final Button btnDelete = new Button();
//            private final HBox pane = new HBox(10, btnEdit, btnDelete);
//
//            {
//                pane.setAlignment(Pos.CENTER_LEFT);
//                btnEdit.getStyleClass().add("btn-action");
//                btnDelete.getStyleClass().add("btn-action");
//
//                loadIcon(btnEdit,   "/image/dashboardform/edit.png");
//                loadIcon(btnDelete, "/image/dashboardform/delete.png");
//
//                btnEdit.setOnAction(e -> {
//                    PartPrices part = getTableView().getItems().get(getIndex());
//                    showEditModal(part);
//                });
//
//                btnDelete.setOnAction(e -> {
//                    PartPrices part = getTableView().getItems().get(getIndex());
//                    confirmDelete(part);
//                });
//            }
//
//            private void loadIcon(Button btn, String path) {
//                try {
//                    URL url = getClass().getResource(path);
//                    if (url != null) {
//                        ImageView iv = new ImageView(new Image(url.toExternalForm()));
//                        iv.setFitHeight(18); iv.setFitWidth(18);
//                        btn.setGraphic(iv);
//                    }
//                } catch (Exception ex) { /* icon không tìm thấy, bỏ qua */ }
//            }
//
//            @Override
//            protected void updateItem(Void item, boolean empty) {
//                super.updateItem(item, empty);
//                setGraphic(empty ? null : pane);
//            }
//        });
//    }
//
//    // ==========================================================
//    // LOAD DỮ LIỆU TỪ DB
//    // ==========================================================
//    private void loadData() {
//        try {
//            List<PartPrices> list = partPriceDAO.findAll();
//            partList = FXCollections.observableArrayList(list);
//            tableParts.setItems(partList);
//            tableParts.setFixedCellSize(60.0);
//
//            updateStats(list);
//            populateVehicleTypeFilter(list);
//
//        } catch (Exception e) {
//            showAlert("Lỗi tải dữ liệu", e.getMessage());
//        }
//    }
//
//    private void updateStats(List<PartPrices> list) {
//        int total = list.size();
//        lblTotalParts.setText(String.valueOf(total));
//        lblCount.setText(total + " phụ tùng");
//
//        double avg = list.stream()
//                .mapToDouble(PartPrices::getPrice)
//                .average().orElse(0);
//        lblAvgPrice.setText(String.format("%,.0f đ", avg).replace(",", "."));
//
//        long distinctTypes = list.stream()
//                .map(PartPrices::getVehicle)
//                .filter(v -> v != null && !v.isBlank())
//                .distinct().count();
//        lblVehicleTypes.setText(String.valueOf(distinctTypes));
//    }
//
//    private void populateVehicleTypeFilter(List<PartPrices> list) {
//        List<String> types = list.stream()
//                .map(PartPrices::getVehicle)
//                .filter(v -> v != null && !v.isBlank())
//                .distinct()
//                .sorted()
//                .collect(Collectors.toList());
//        types.add(0, "Tất cả");
//        cbVehicleType.setItems(FXCollections.observableArrayList(types));
//        cbVehicleType.getSelectionModel().selectFirst();
//    }
//
//    // ==========================================================
//    // TÌM KIẾM & FILTER
//    // ==========================================================
//    @FXML
//    void handleSearchLive() {
//        applyFilter();
//    }
//
//    @FXML
//    void handleSearch(ActionEvent event) {
//        applyFilter();
//    }
//
//    @FXML
//    void handleFilter(ActionEvent event) {
//        applyFilter();
//    }
//
//    private void applyFilter() {
//        String keyword     = txtSearch.getText().trim().toLowerCase();
//        String vehicleType = cbVehicleType.getValue();
//
//        ObservableList<PartPrices> filtered = partList.filtered(p -> {
//            boolean matchKeyword = keyword.isEmpty() ||
//                    (p.getPart_name() != null && p.getPart_name().toLowerCase().contains(keyword));
//            boolean matchType = vehicleType == null || vehicleType.equals("Tất cả") ||
//                    vehicleType.equals(p.getVehicle());
//            return matchKeyword && matchType;
//        });
//
//        tableParts.setItems(filtered);
//        lblCount.setText(filtered.size() + " phụ tùng");
//    }
//
//    @FXML
//    void handleReload(ActionEvent event) {
//        txtSearch.clear();
//        cbVehicleType.getSelectionModel().selectFirst();
//        loadData();
//    }
//
//    // ==========================================================
//    // MỞ MODAL THÊM MỚI
//    // ==========================================================
//    @FXML
//    void showAddPartModal(ActionEvent event) {
//        try {
//            FXMLLoader loader = new FXMLLoader(
//                    getClass().getResource("/views/part/PartFormModal.fxml"));
//            Parent root = loader.load();
//
//            PartFormController controller = loader.getController();
//            controller.setMode(PartFormController.Mode.ADD, null);
//            controller.setOnSaved(this::loadData);
//
//            Stage stage = new Stage();
//            stage.setScene(new Scene(root));
//            stage.setTitle("Thêm phụ tùng mới");
//            stage.initModality(Modality.APPLICATION_MODAL);
//            stage.setResizable(false);
//            stage.centerOnScreen();
//            stage.showAndWait();
//
//        } catch (Exception e) {
//            showAlert("Lỗi mở form", e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    // ==========================================================
//    // MỞ MODAL SỬA
//    // ==========================================================
//    private void showEditModal(PartPrices part) {
//        try {
//            FXMLLoader loader = new FXMLLoader(
//                    getClass().getResource("/views/part/PartFormModal.fxml"));
//            Parent root = loader.load();
//
//            PartFormController controller = loader.getController();
//            controller.setMode(PartFormController.Mode.EDIT, part);
//            controller.setOnSaved(this::loadData);
//
//            Stage stage = new Stage();
//            stage.setScene(new Scene(root));
//            stage.setTitle("Sửa phụ tùng: " + part.getPart_name());
//            stage.initModality(Modality.APPLICATION_MODAL);
//            stage.setResizable(false);
//            stage.centerOnScreen();
//            stage.showAndWait();
//
//        } catch (Exception e) {
//            showAlert("Lỗi mở form sửa", e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    // ==========================================================
//    // XÁC NHẬN XÓA
//    // ==========================================================
//    private void confirmDelete(PartPrices part) {
//        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
//        confirm.setTitle("Xác nhận xóa");
//        confirm.setHeaderText(null);
//        confirm.setContentText("Bạn có chắc muốn xóa phụ tùng:\n\"" + part.getPart_name() + "\"?");
//
//        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
//            boolean success = partPriceDAO.delete(part.getId_part_price());
//            if (success) {
//                loadData();
//            } else {
//                showAlert("Lỗi", "Không thể xóa phụ tùng này!");
//            }
//        });
//    }
//
//    // ==========================================================
//    // TIỆN ÍCH
//    // ==========================================================
//    private void showAlert(String title, String message) {
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//}