package com.example.rentalcar.controller.dashboard;

import com.example.rentalcar.controller.contract.ContractDetailController;
import com.example.rentalcar.dao.ContractDAO;
import com.example.rentalcar.dao.CustomerDAO;
import com.example.rentalcar.dao.VehicleDAO;
import com.example.rentalcar.models.Contracts;
import com.example.rentalcar.models.Customers;
import com.example.rentalcar.models.Vehicles;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;

public class DashboardController {

    @FXML private Label label_status;
    @FXML private Label label_status_available;
    @FXML private Label label_today_revenue;

    // DAO
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final ContractDAO contractDAO = new ContractDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    // Table
    @FXML private TableView<Contracts> tableContracts;
    @FXML private TableColumn<Contracts, String> colCode;
    @FXML private TableColumn<Contracts, String> colCustomer;
    @FXML private TableColumn<Contracts, String> colVehicle;
    @FXML private TableColumn<Contracts, String> colStatus;
    @FXML private TableColumn<Contracts, String> colTotal;
    @FXML private TableColumn<Contracts, Void> colAction;

    @FXML
    public void initialize() {
        loadVehicleStatus();
        loadContractStatus();
        setupRecentContractsTable();
    }

    private void loadVehicleStatus() {
        try {
            int total = vehicleDAO.getTotalActiveCars();
            int rented = vehicleDAO.getRentedCars();
            int available = vehicleDAO.getAvailableCars();

            if (label_status != null) label_status.setText(rented + "/" + total);
            if (label_status_available != null) label_status_available.setText("✅ " + available + " xe đang sẵn sàng");
        } catch (Exception e) {
            System.err.println("Lỗi nạp thống kê xe: " + e.getMessage());
        }
    }

    private void loadContractStatus() {
        try {
            double total = contractDAO.getTodayRevenue();
            if (label_today_revenue != null) {
                label_today_revenue.setText(String.format("%,.0f đ", total).replace(",", "."));
            }
        } catch (Exception e) {
            System.err.println("Lỗi nạp doanh thu: " + e.getMessage());
        }
    }
    // Bảng hợp đồng gần đây
    private void setupRecentContractsTable() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code_contract"));
        colTotal.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal_price();
            return new SimpleStringProperty(String.format("%,.0f đ", total).replace(",", "."));
        });

        colCustomer.setCellValueFactory(cellData -> {
            if (cellData.getValue().getId_customer() != null) {
                int id = cellData.getValue().getId_customer().getId_customer();
                Customers c = customerDAO.findById(id);
                return new SimpleStringProperty(c != null ? c.getFull_name() : "Khách vãng lai");
            }
            return new SimpleStringProperty("Khách vãng lai");
        });

        colVehicle.setCellValueFactory(cellData -> {
            if (cellData.getValue().getId_vehicle() != null) {
                int id = cellData.getValue().getId_vehicle().getId_vehicle();
                Vehicles v = vehicleDAO.findById(id);
                return new SimpleStringProperty(v != null ? v.getCode_vehicle() : "Không rõ");
            }
            return new SimpleStringProperty("Không rõ");
        });

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Contracts contract = getTableRow().getItem();
                    Label pill = new Label();
                    pill.getStyleClass().add("status-pill");

                    if (contract.getStatus() != null) {
                        String st = contract.getStatus().name();
                        if (st.equals("DANG_THUE")) {
                            pill.setText("Đang thuê");
                            pill.getStyleClass().add("status-blue");
                        } else if (st.equals("HOAN_THANH")) {
                            pill.setText("Hoàn thành");
                            pill.getStyleClass().add("status-green");
                        } else {
                            pill.setText(st);
                            pill.getStyleClass().add("status-gray");
                        }
                    }
                    setGraphic(pill);
                }
            }
        });

        colAction.setCellFactory(column -> new TableCell<>() {
            private final Button btnView = new Button();
            private final Button btnPrint = new Button();
            private final HBox pane = new HBox(12, btnView, btnPrint);

            {
                pane.setStyle("-fx-alignment: CENTER_LEFT;");
                btnView.getStyleClass().add("btn-action");
                btnPrint.getStyleClass().add("btn-action");

                loadIcon(btnView, "/image/dashboardform/view.png");
                loadIcon(btnPrint, "/image/dashboardform/printer.png");

                // Sự kiện nút Xem -> Mở chi tiết
                btnView.setOnAction(e -> {
                    Contracts selected = getTableView().getItems().get(getIndex());
                    showDetailPopup(selected);
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
                } catch (Exception ex) { System.err.println("Lỗi load icon: " + path); }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        tableContracts.setItems(FXCollections.observableArrayList(contractDAO.findRecentContracts(5)));
    }
    // Xem chi tiết
    private void showDetailPopup(Contracts contract) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/contract/ContractDetailView.fxml"));
            Parent root = loader.load();

            ContractDetailController controller = loader.getController();
            controller.setContractData(contract);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi tiết: " + contract.getCode_contract());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Lỗi load FXML chi tiết: " + e.getMessage());
        }
    }

    //Tạo hợp đồng mới
    @FXML
    void showCreateContractModal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/create_contract/MainLayout.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Tạo hợp đồng mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            System.err.println("Lỗi mở màn hình tạo hợp đồng: " + e.getMessage());
        }
    }
}