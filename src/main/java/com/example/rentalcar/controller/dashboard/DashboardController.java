package com.example.rentalcar.controller.dashboard;

import com.example.rentalcar.bll.ContractBLL;
import com.example.rentalcar.bll.CustomerBLL;
import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.controller.contract.ContractDetailController;
import com.example.rentalcar.models.Contracts;
import com.example.rentalcar.models.Customers;
import com.example.rentalcar.models.StatusContracts;
import com.example.rentalcar.models.Vehicles;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    // ===== CARDS =====
    @FXML private Label label_today_revenue;
    @FXML private Label label_monthly_revenue;
    @FXML private Label label_active_contracts;
    @FXML private Label label_status;
    @FXML private Label label_status_available;

    // ===== ALERTS =====
    @FXML private Label label_overdue_count;
    @FXML private Label label_maintenance_count;
    @FXML private Label label_voucher_warning;
    @FXML private Label label_recent_count;

    // ===== TABLE =====
    @FXML private TableView<Contracts>            tableContracts;
    @FXML private TableColumn<Contracts, String>  colCode;
    @FXML private TableColumn<Contracts, String>  colCustomer;
    @FXML private TableColumn<Contracts, String>  colVehicle;
    @FXML private TableColumn<Contracts, String>  colStatus;
    @FXML private TableColumn<Contracts, String>  colTotal;
    @FXML private TableColumn<Contracts, Void>    colAction;

    private final VehicleBLL  vehicleBLL  = new VehicleBLL();
    private final ContractBLL contractBLL = new ContractBLL();
    private final CustomerBLL customerBLL = new CustomerBLL();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    @FXML
    public void initialize() {
        loadCards();
        setupTable();
        loadRecentContracts();

        // Tự động kiểm tra xe đạt km bảo dưỡng khi mở dashboard
        checkMaintenanceOnStartup();
    }

    // ==========================================
    // KIỂM TRA BẢO DƯỠNG KHI MỞ APP
    // ==========================================
    private void checkMaintenanceOnStartup() {
        try {
            List<Vehicles> justMarked = vehicleBLL.checkAndMarkMaintenance();

            if (!justMarked.isEmpty()) {
                // Tạo nội dung thông báo
                StringBuilder sb = new StringBuilder();
                sb.append("Các xe sau đã đạt mốc km bảo dưỡng và được chuyển sang\n");
                sb.append("trạng thái BẢO DƯỠNG:\n\n");

                for (Vehicles v : justMarked) {
                    sb.append("🔧 ").append(v.getCode_vehicle())
                            .append(" — ").append(v.getBrand()).append(" ").append(v.getModel())
                            .append("  (").append(v.getCurrent_km()).append(" km)\n");
                }

                sb.append("\nVui lòng tiến hành bảo dưỡng và cập nhật trong\nmàn hình Quản lý xe.");

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("⚠ Xe cần bảo dưỡng");
                alert.setHeaderText("Phát hiện " + justMarked.size() + " xe đạt mốc bảo dưỡng!");
                alert.setContentText(sb.toString());
                alert.showAndWait();

                // Reload cards để cập nhật số liệu
                loadCards();
            }
        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra bảo dưỡng: " + e.getMessage());
        }
    }

    // ==========================================
    // LOAD CARDS
    // ==========================================
    private void loadCards() {
        try {
            double todayRev = contractBLL.getTodayRevenue();
            if (label_today_revenue != null)
                label_today_revenue.setText(formatMoney(todayRev));

            double monthRev = contractBLL.getMonthlyRevenue();
            if (label_monthly_revenue != null)
                label_monthly_revenue.setText(formatMoney(monthRev));

            int activeCount = contractBLL.getActiveContractsCount();
            if (label_active_contracts != null)
                label_active_contracts.setText(String.valueOf(activeCount));

            int total     = vehicleBLL.getTotalActiveCars();
            int rented    = vehicleBLL.getRentedCars();
            int available = vehicleBLL.getAvailableCars();
            if (label_status != null)
                label_status.setText(rented + " / " + total);
            if (label_status_available != null)
                label_status_available.setText("✅  " + available + " xe sẵn sàng");

            // Cập nhật số xe đang bảo dưỡng lên alert banner
            int maintenanceCount = vehicleBLL.getMaintenanceVehicles().size();
            if (label_maintenance_count != null) {
                if (maintenanceCount > 0) {
                    label_maintenance_count.setText(
                            maintenanceCount + " xe đang trong trạng thái bảo dưỡng");
                    label_maintenance_count.setStyle("-fx-text-fill: #d97706; -fx-font-size: 13px;");
                } else {
                    label_maintenance_count.setText("Không có xe nào đang bảo dưỡng");
                    label_maintenance_count.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
                }
            }

        } catch (Exception e) {
            System.err.println("Lỗi load cards: " + e.getMessage());
        }
    }

    // ==========================================
    // SETUP TABLE COLUMNS
    // ==========================================
    private void setupTable() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code_contract"));
        colCode.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String code, boolean empty) {
                super.updateItem(code, empty);
                if (empty || code == null) { setGraphic(null); return; }
                Label lbl = new Label(code);
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #146dff; -fx-font-size: 14px;");
                setGraphic(lbl);
            }
        });

        colCustomer.setCellValueFactory(cell -> {
            if (cell.getValue().getId_customer() == null) return new SimpleStringProperty("--");
            Customers c = customerBLL.findById(cell.getValue().getId_customer().getId_customer());
            return new SimpleStringProperty(c != null ? c.getFull_name() : "--");
        });
        colCustomer.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null || name.equals("--")) { setGraphic(null); return; }
                Circle av = new Circle(16, Color.web("#4f46e5"));
                Text ini = new Text(name.substring(0, 1).toUpperCase());
                ini.setFill(Color.WHITE);
                ini.setStyle("-fx-font-weight: bold;");
                javafx.scene.layout.StackPane sp = new javafx.scene.layout.StackPane(av, ini);
                Label lbl = new Label(name);
                lbl.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 13px;");
                HBox box = new HBox(10, sp, lbl);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        colVehicle.setCellValueFactory(cell -> {
            if (cell.getValue().getId_vehicle() == null) return new SimpleStringProperty("--");
            Vehicles v = vehicleBLL.getVehicleById(cell.getValue().getId_vehicle().getId_vehicle());
            return new SimpleStringProperty(v != null ? v.getCode_vehicle() : "--");
        });
        colVehicle.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String plate, boolean empty) {
                super.updateItem(plate, empty);
                if (empty || plate == null) { setGraphic(null); return; }
                Label lbl = new Label(plate);
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;" +
                        " -fx-background-color: #f1f5f9; -fx-padding: 3 10;" +
                        " -fx-background-radius: 6; -fx-font-size: 13px;");
                setGraphic(lbl);
            }
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Contracts c = getTableRow().getItem();
                Label pill = new Label();
                pill.getStyleClass().add("status-pill");
                if (c.getStatus() != null) {
                    switch (c.getStatus()) {
                        case DANG_THUE  -> { pill.setText("Đang thuê");  pill.getStyleClass().add("status-blue"); }
                        case HOAN_THANH -> { pill.setText("Hoàn thành"); pill.getStyleClass().add("status-green"); }
                        case QUA_HAN    -> { pill.setText("Quá hạn");    pill.getStyleClass().add("status-yellow"); }
                        case DA_HUY     -> { pill.setText("Đã hủy");     pill.getStyleClass().add("status-gray"); }
                    }
                }
                setGraphic(pill);
            }
        });

        colTotal.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        String.format("%,.0f đ", cell.getValue().getTotal_price()).replace(",", ".")
                )
        );
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) { setGraphic(null); return; }
                Label lbl = new Label(total);
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 13px;");
                setGraphic(lbl);
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnView  = new Button();
            private final Button btnPrint = new Button();
            private final HBox   pane     = new HBox(10, btnView, btnPrint);
            {
                pane.setAlignment(Pos.CENTER_LEFT);
                btnView.getStyleClass().add("btn-action");
                btnPrint.getStyleClass().add("btn-action");
                loadIcon(btnView,  "/image/dashboardform/view.png");
                loadIcon(btnPrint, "/image/dashboardform/printer.png");
                btnView.setOnAction(e -> showDetailPopup(
                        getTableView().getItems().get(getIndex())));
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

    // ==========================================
    // LOAD DỮ LIỆU BẢNG
    // ==========================================
    private void loadRecentContracts() {
        try {
            List<Contracts> list = contractBLL.getRecentContracts(5);
            tableContracts.setItems(FXCollections.observableArrayList(list));
            tableContracts.setFixedCellSize(60.0);
            if (label_recent_count != null)
                label_recent_count.setText(list.size() + " đơn gần nhất");
        } catch (Exception e) {
            System.err.println("Lỗi load bảng: " + e.getMessage());
        }
    }

    // ==========================================
    // POPUP CHI TIẾT HĐ
    // ==========================================
    private void showDetailPopup(Contracts contract) {
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
            stage.show();
        } catch (IOException e) {
            System.err.println("Lỗi mở chi tiết: " + e.getMessage());
        }
    }

    // ==========================================
    // MỞ TẠO HĐ
    // ==========================================
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
            stage.setOnHidden(e -> loadRecentContracts());
            stage.show();
        } catch (Exception e) {
            System.err.println("Lỗi mở form: " + e.getMessage());
        }
    }

    // ==========================================
    // HELPER
    // ==========================================
    private String formatMoney(double amount) {
        return String.format("%,.0f đ", amount).replace(",", ".");
    }
}