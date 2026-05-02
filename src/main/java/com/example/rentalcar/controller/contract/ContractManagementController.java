package com.example.rentalcar.controller.contract;

import com.example.rentalcar.bll.ContractBLL;
import com.example.rentalcar.bll.CustomerBLL;
import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.models.Contracts;
import com.example.rentalcar.models.Customers;
import com.example.rentalcar.models.StatusContracts;
import com.example.rentalcar.models.Vehicles;
import com.example.rentalcar.utils.AppSession;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ContractManagementController implements Initializable {

    // ======= CARDS =======
    @FXML private Label lblTotalContracts;
    @FXML private Label lblActiveContracts;
    @FXML private Label lblOverdueContracts;
    @FXML private Label lblMonthlyRevenue;
    @FXML private Label lblSubtitle;
    @FXML private Label lblCount;

    // ======= FILTER =======
    @FXML private ComboBox<String> cbStatusFilter;

    // ======= SEARCH =======
    @FXML private TextField txtSearch;

    // ======= TABLE =======
    @FXML private TableView<Contracts>            tableContracts;
    @FXML private TableColumn<Contracts, String>  colCode;
    @FXML private TableColumn<Contracts, String>  colCustomer;
    @FXML private TableColumn<Contracts, String>  colVehicle;
    @FXML private TableColumn<Contracts, String>  colPeriod;
    @FXML private TableColumn<Contracts, String>  colStatus;
    @FXML private TableColumn<Contracts, String>  colPayment;
    @FXML private TableColumn<Contracts, String>  colTotal;
    @FXML private TableColumn<Contracts, Void>    colAction;

    // ======= BLL =======
    private final ContractBLL contractBLL = new ContractBLL();
    private final CustomerBLL customerBLL = new CustomerBLL();
    private final VehicleBLL  vehicleBLL  = new VehicleBLL();

    private ObservableList<Contracts> masterList;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();

        cbStatusFilter.getItems().addAll("Tất cả", "Đang thuê", "Quá hạn", "Hoàn thành", "Đã hủy");
        cbStatusFilter.setValue("Tất cả");

        loadData();
    }

    private void setupTableColumns() {

        // Mã hợp đồng
        colCode.setCellValueFactory(new PropertyValueFactory<>("code_contract"));
        colCode.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String code, boolean empty) {
                super.updateItem(code, empty);
                if (empty || code == null) { setGraphic(null); return; }
                Label lbl = new Label(code);
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #146dff; -fx-font-size: 14px;");
                setGraphic(lbl);
            }
        });

        // Khách hàng
        colCustomer.setCellValueFactory(cell -> {
            if (cell.getValue().getId_customer() == null) return new SimpleStringProperty("--");
            Customers c = customerBLL.findById(cell.getValue().getId_customer().getId_customer());
            return new SimpleStringProperty(c != null ? c.getFull_name() : "--");
        });
        colCustomer.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null || name.equals("--")) { setGraphic(null); return; }
                Circle avatar = new Circle(16, Color.web("#4f46e5"));
                Text initial = new Text(name.substring(0, 1).toUpperCase());
                initial.setFill(Color.WHITE);
                initial.setStyle("-fx-font-weight: bold;");
                javafx.scene.layout.StackPane sp = new javafx.scene.layout.StackPane(avatar, initial);
                Label lbl = new Label(name);
                lbl.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 13px;");
                HBox box = new HBox(10, sp, lbl);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        // Xe thuê
        colVehicle.setCellValueFactory(cell -> {
            if (cell.getValue().getId_vehicle() == null) return new SimpleStringProperty("--");
            Vehicles v = vehicleBLL.getVehicleById(cell.getValue().getId_vehicle().getId_vehicle());
            if (v == null) return new SimpleStringProperty("--");
            return new SimpleStringProperty(v.getBrand() + " " + v.getModel() + "\n" + v.getCode_vehicle());
        });
        colVehicle.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String info, boolean empty) {
                super.updateItem(info, empty);
                if (empty || info == null) { setGraphic(null); return; }
                String[] parts = info.split("\n");
                Label lblModel = new Label(parts.length > 0 ? parts[0] : "");
                lblModel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");
                Label lblPlate = new Label(parts.length > 1 ? parts[1] : "");
                lblPlate.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; "
                        + "-fx-background-color: #f1f5f9; -fx-padding: 2 8; -fx-background-radius: 6;");
                VBox vb = new VBox(2, lblModel, lblPlate);
                vb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(vb);
            }
        });

        // Thời gian thuê
        colPeriod.setCellValueFactory(cell -> {
            Contracts c = cell.getValue();
            if (c.getStart_datetime() == null || c.getEnd_datetime() == null)
                return new SimpleStringProperty("--");
            return new SimpleStringProperty(
                    c.getStart_datetime().format(FMT) + " →\n" + c.getEnd_datetime().format(FMT));
        });
        colPeriod.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String period, boolean empty) {
                super.updateItem(period, empty);
                if (empty || period == null) { setGraphic(null); return; }
                String[] parts = period.split("→\n");
                Label start = new Label(parts.length > 0 ? parts[0].trim() : "");
                start.setStyle("-fx-font-size: 12px; -fx-text-fill: #1e293b;");
                Label end = new Label(parts.length > 1 ? parts[1].trim() : "");
                end.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
                VBox vb = new VBox(3, start, end);
                vb.setAlignment(Pos.CENTER_LEFT);
                setGraphic(vb);
            }
        });

        // Trạng thái hợp đồng
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Contracts contract = (Contracts) getTableRow().getItem();
                if (contract.getStatus() == null) { setGraphic(null); return; }
                Label pill = new Label();
                pill.getStyleClass().add("status-pill");
                switch (contract.getStatus()) {
                    case DANG_THUE  -> { pill.setText("Đang thuê");  pill.getStyleClass().add("status-blue"); }
                    case QUA_HAN    -> { pill.setText("Quá hạn");    pill.getStyleClass().add("status-yellow"); }
                    case HOAN_THANH -> { pill.setText("Hoàn thành"); pill.getStyleClass().add("status-green"); }
                    case DA_HUY     -> { pill.setText("Đã hủy");     pill.getStyleClass().add("status-gray"); }
                }
                HBox box = new HBox(pill);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        // Thanh toán
        colPayment.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Contracts contract = (Contracts) getTableRow().getItem();
                if (contract.getPayment_status() == null) { setGraphic(null); return; }
                Label pill = new Label();
                pill.getStyleClass().add("pay-pill");
                switch (contract.getPayment_status()) {
                    case CHUA_THANH_TOAN   -> { pill.setText("Chưa TT");  pill.getStyleClass().add("pay-none"); }
                    case THANH_TOAN_1_PHAN -> { pill.setText("Một phần"); pill.getStyleClass().add("pay-part"); }
                    case DA_THANH_TOAN     -> { pill.setText("Đã TT");    pill.getStyleClass().add("pay-done"); }
                }
                HBox box = new HBox(pill);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        // Tổng tiền
        colTotal.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        String.format("%,.0f đ", cell.getValue().getTotal_price()).replace(",", ".")));
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) { setGraphic(null); return; }
                Label lbl = new Label(total);
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 13px;");
                setGraphic(lbl);
            }
        });

        // ── Cột thao tác: Xem | In | Hủy ───────────────────────────
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnView   = new Button();
            private final Button btnPrint  = new Button();
            private final Button btnCancel = new Button();
            private final HBox   pane      = new HBox(6, btnView, btnPrint, btnCancel);

            {
                pane.setAlignment(Pos.CENTER_LEFT);
                btnView.getStyleClass().add("btn-action");
                btnPrint.getStyleClass().add("btn-action");
                btnCancel.getStyleClass().add("btn-action");

                loadIcon(btnView,   "/image/dashboardform/view.png");
                loadIcon(btnPrint,  "/image/dashboardform/printer.png");
                loadIcon(btnCancel, "/image/dashboardform/delete.png");

                btnView.setOnAction(e -> {
                    Contracts c = getTableView().getItems().get(getIndex());
                    showDetailModal(c);
                });
                btnPrint.setOnAction(e -> {
                    showInfo("In hợp đồng " + getTableView().getItems().get(getIndex()).getCode_contract());
                });
                btnCancel.setOnAction(e -> {
                    Contracts c = getTableView().getItems().get(getIndex());
                    confirmCancelContract(c);
                });
            }

            private void loadIcon(Button btn, String path) {
                try {
                    URL url = getClass().getResource(path);
                    if (url != null) {
                        ImageView iv = new ImageView(new Image(url.toExternalForm()));
                        iv.setFitHeight(17); iv.setFitWidth(17);
                        btn.setGraphic(iv);
                    }
                } catch (Exception ignored) {}
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }

                Contracts c = getTableView().getItems().get(getIndex());
                boolean canCancel = false;

                if (c.getStatus() == StatusContracts.DANG_THUE
                        || c.getStatus() == StatusContracts.QUA_HAN) {
                    if (AppSession.isAdmin()) {
                        // Admin hủy được tất cả HĐ
                        canCancel = true;
                    } else if (AppSession.isStaff() && c.getId_user() != null
                            && AppSession.getCurrentUser() != null
                            && c.getId_user().getId_user() == AppSession.getCurrentUser().getId_user()) {
                        // Staff chỉ hủy HĐ do mình tạo
                        canCancel = true;
                    }
                }
                btnCancel.setVisible(canCancel);
                btnCancel.setManaged(canCancel);
                setGraphic(pane);
            }
        });
    }

    private void loadData() {
        try {
            List<Contracts> list = contractBLL.getAllContracts();
            masterList = FXCollections.observableArrayList(list);

            tableContracts.setItems(masterList);
            tableContracts.setFixedCellSize(68.0);
            lblCount.setText(masterList.size() + " hợp đồng");

            updateStats(list);
        } catch (Exception e) {
            showError("Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void updateStats(List<Contracts> list) {
        lblTotalContracts.setText(String.valueOf(list.size()));

        long active  = list.stream().filter(c -> c.getStatus() == StatusContracts.DANG_THUE).count();
        long overdue = list.stream().filter(c -> c.getStatus() == StatusContracts.QUA_HAN).count();
        lblActiveContracts.setText(String.valueOf(active));
        lblOverdueContracts.setText(String.valueOf(overdue));

        double revenue = contractBLL.getMonthlyRevenue();
        lblMonthlyRevenue.setText(String.format("%,.0f đ", revenue).replace(",", "."));
    }

    @FXML
    void handleSearch(ActionEvent event) {
        if (masterList == null) return;
        String keyword       = txtSearch.getText().trim().toLowerCase();
        String selectedStatus = cbStatusFilter.getValue();

        ObservableList<Contracts> filtered = masterList.filtered(c -> {
            boolean matchStatus = false;
            if (selectedStatus == null || selectedStatus.equals("Tất cả")) {
                matchStatus = true;
            } else if (selectedStatus.equals("Đang thuê")  && c.getStatus() == StatusContracts.DANG_THUE)  { matchStatus = true; }
            else if (selectedStatus.equals("Quá hạn")    && c.getStatus() == StatusContracts.QUA_HAN)    { matchStatus = true; }
            else if (selectedStatus.equals("Hoàn thành") && c.getStatus() == StatusContracts.HOAN_THANH) { matchStatus = true; }
            else if (selectedStatus.equals("Đã hủy")     && c.getStatus() == StatusContracts.DA_HUY)     { matchStatus = true; }

            boolean matchKeyword = keyword.isEmpty()
                    || (c.getCode_contract() != null && c.getCode_contract().toLowerCase().contains(keyword))
                    || (c.getId_customer() != null && tryGetCustomerName(c).toLowerCase().contains(keyword));

            return matchStatus && matchKeyword;
        });

        tableContracts.setItems(filtered);
        lblCount.setText(filtered.size() + " hợp đồng");
    }

    private String tryGetCustomerName(Contracts c) {
        try {
            Customers cu = customerBLL.findById(c.getId_customer().getId_customer());
            return cu != null ? cu.getFull_name() : "";
        } catch (Exception e) { return ""; }
    }

    @FXML
    void handleReload(ActionEvent event) {
        txtSearch.clear();
        cbStatusFilter.setValue("Tất cả");
        loadData();
    }

    @FXML
    void showCreateContractModal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/create_contract/MainLayout.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Tạo hợp đồng mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHidden(e -> loadData());
            stage.show();
        } catch (Exception e) {
            showError("Lỗi mở form tạo hợp đồng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showDetailModal(Contracts contract) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/contract/ContractDetailView.fxml"));
            Parent root = loader.load();
            ContractDetailController ctrl = loader.getController();
            ctrl.setContractData(contract);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi tiết: " + contract.getCode_contract());
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> loadData());
            stage.show();
        } catch (Exception e) {
            showError("Lỗi mở chi tiết: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void confirmCancelContract(Contracts contract) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy hợp đồng");
        confirm.setHeaderText(null);
        confirm.setContentText("Hủy hợp đồng: " + contract.getCode_contract() + "?\nHành động này không thể khôi phục!");

        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                contractBLL.cancelContract(contract.getId_contract());
                loadData();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi"); alert.setHeaderText(null); alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo"); alert.setHeaderText(null); alert.setContentText(message);
        alert.showAndWait();
    }
}