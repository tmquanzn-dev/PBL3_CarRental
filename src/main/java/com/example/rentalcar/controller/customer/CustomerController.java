package com.example.rentalcar.controller.customer;

import com.example.rentalcar.bll.CustomerBLL;
import com.example.rentalcar.models.Customers;
import com.example.rentalcar.utils.AppSession;
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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {

    // ── Cards ────────────────────────────────────────────
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblNewCustomers;
    @FXML private Label lblBlacklist;

    // ── Search ────────────────────────────────────────────
    @FXML private TextField        txtSearch;
    @FXML private ComboBox<String> cbFilter;

    // ── Table ─────────────────────────────────────────────
    @FXML private TableView<Customers>            tableCustomers;
    @FXML private TableColumn<Customers, String>  colCccd;
    @FXML private TableColumn<Customers, String>  colName;
    @FXML private TableColumn<Customers, String>  colPhone;
    @FXML private TableColumn<Customers, Integer> colRentalCount;
    @FXML private TableColumn<Customers, Boolean> colStatus;
    @FXML private TableColumn<Customers, Void>    colAction;

    private final CustomerBLL customerBLL = new CustomerBLL();
    private ObservableList<Customers> masterList;

    //
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilter();
        setupTableColumns();
        loadData();
        tableCustomers.setFixedCellSize(60.0);
    }

    //Filter ComboBox
    private void setupFilter() {
        if (cbFilter != null) {
            cbFilter.getItems().addAll("Tất cả", "Bình thường", "Blacklist");
            cbFilter.setValue("Tất cả");
            cbFilter.setOnAction(e -> applyFilter());
        }
    }

    //Table Columns
    private void setupTableColumns() {
        colCccd.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getCccd()));

        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colName.setCellValueFactory(new PropertyValueFactory<>("full_name"));
        colName.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null || name.isBlank()) {
                    setGraphic(null); return;
                }
                Circle avatar = new Circle(15, Color.web("#478dff"));
                Text initial = new Text(name.substring(0, 1).toUpperCase());
                initial.setFill(Color.WHITE);
                StackPane sp = new StackPane(avatar, initial);
                Label lbl = new Label(name);
                HBox box = new HBox(12, sp, lbl);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
                setText(null);
            }
        });

        // Cột số lần thuê
        colRentalCount.setCellValueFactory(new PropertyValueFactory<>("rental_count"));
        colRentalCount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer count, boolean empty) {
                super.updateItem(count, empty);
                if (empty || count == null) { setGraphic(null); return; }
                Label badge = new Label(count + " lần");
                badge.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca; "
                        + "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: bold;");
                HBox box = new HBox(badge);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        // Cột trạng thái
        colStatus.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleBooleanProperty(cd.getValue().isIs_blacklist()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean isBlacklist, boolean empty) {
                super.updateItem(isBlacklist, empty);
                if (empty || isBlacklist == null) { setGraphic(null); return; }
                Label pill = new Label(isBlacklist ? "⛔ Blacklist" : "✅ Bình thường");
                pill.getStyleClass().add("status-pill");
                pill.getStyleClass().add(isBlacklist ? "status-yellow" : "status-green");
                HBox box = new HBox(pill);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnView      = new Button();
            private final Button btnEdit      = new Button();
            private final Button btnBlacklist = new Button();
            private final HBox   pane         = new HBox(10, btnView, btnEdit, btnBlacklist);

            {
                pane.setAlignment(Pos.CENTER_LEFT);
                btnView.getStyleClass().add("btn-action");
                btnEdit.getStyleClass().add("btn-action");
                btnBlacklist.getStyleClass().add("btn-action");

                loadIcon(btnView, "/icon/dashboardform/view.png");
                loadIcon(btnEdit, "/icon/dashboardform/edit.png");

                //chỉ Admin blacklist được
                btnBlacklist.setVisible(AppSession.isAdmin());
                btnBlacklist.setManaged(AppSession.isAdmin());

                btnView.setOnAction(e -> {
                    Customers c = getTableRow().getItem();
                    if (c != null) showDetailModal(c);
                });
                btnEdit.setOnAction(e -> {
                    Customers c = getTableRow().getItem();
                    if (c != null) showEditModal(c);
                });
                btnBlacklist.setOnAction(e -> {
                    if (!AppSession.isAdmin()) return;
                    Customers c = getTableRow().getItem();
                    if (c != null) handleBlacklistToggle(c);
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

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); return; }

                Customers c = getTableRow().getItem();
                if (AppSession.isAdmin()) {
                    if (c.isIs_blacklist()) {
                        btnBlacklist.setText("✅ Gỡ BL");
                        btnBlacklist.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; "
                                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 4 8;");
                    } else {
                        btnBlacklist.setText("⛔ BL");
                        btnBlacklist.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; "
                                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 4 8;");
                    }
                    btnBlacklist.setGraphic(null);
                }
                setGraphic(pane);
            }
        });
    }

    //Load Data
    private void loadData() {
        try {
            List<Customers> list = customerBLL.getAllCustomers();
            if (list == null) return;
            masterList = FXCollections.observableArrayList(list);
            tableCustomers.setItems(masterList);
            updateStats(list);
        } catch (Exception e) {
            showError("Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void updateStats(List<Customers> list) {
        lblTotalCustomers.setText(String.valueOf(list.size()));
        long bl = list.stream().filter(Customers::isIs_blacklist).count();
        lblBlacklist.setText(String.valueOf(bl));
        lblNewCustomers.setText("0"); // TODO: tính theo tháng
    }

    //Search & Filter
    @FXML
    void handleSearch(ActionEvent event) {
        applyFilter();
    }

    private void applyFilter() {
        if (masterList == null) return;
        String keyword   = txtSearch != null ? txtSearch.getText().trim().toLowerCase() : "";
        String filterVal = cbFilter  != null ? cbFilter.getValue() : "Tất cả";

        ObservableList<Customers> filtered = masterList.filtered(c -> {
            boolean matchKey = keyword.isEmpty()
                    || c.getFull_name().toLowerCase().contains(keyword)
                    || c.getCccd().toLowerCase().contains(keyword)
                    || (c.getPhone() != null && c.getPhone().contains(keyword));

            boolean matchFilter = "Tất cả".equals(filterVal)
                    || ("Blacklist".equals(filterVal)    && c.isIs_blacklist())
                    || ("Bình thường".equals(filterVal) && !c.isIs_blacklist());

            return matchKey && matchFilter;
        });

        tableCustomers.setItems(filtered);
    }

    // Blacklist Toggle
    private void handleBlacklistToggle(Customers customer) {
        if (customer == null) return;

        if (customer.isIs_blacklist()) {
            // Gỡ khỏi blacklist
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Gỡ khỏi danh sách đen");
            confirm.setHeaderText(null);
            confirm.setContentText("Bạn có chắc muốn gỡ \"" + customer.getFull_name() + "\" khỏi danh sách đen?");
            confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                try {
                    customerBLL.removeFromBlacklist(customer.getId_customer());
                    loadData();
                    showInfo("Đã gỡ khách hàng khỏi danh sách đen thành công!");
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        } else {
            // Thêm vào blacklist → mở modal nhập lý do
            showBlacklistModal(customer);
        }
    }

    private void showBlacklistModal(Customers customer) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/customer/BlacklistModal.fxml"));
            Parent root = loader.load();

            BlacklistModalController ctrl = loader.getController();
            ctrl.setCustomer(customer, result -> {
                if (result) loadData();
            });

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Thêm vào danh sách đen");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (Exception e) {
            // Fallback: TextInputDialog nếu chưa có FXML
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Thêm vào danh sách đen");
            dialog.setHeaderText("Khách hàng: " + customer.getFull_name());
            dialog.setContentText("Nhập lý do:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(reason -> {
                if (reason.isBlank()) { showError("Lý do không được để trống!"); return; }
                try {
                    customerBLL.addToBlacklist(customer.getId_customer(), reason);
                    loadData();
                    showInfo("Đã thêm vào danh sách đen!");
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        }
    }

    // ── Modals ────────────────────────────────────────────
    // Staff và Admin đều được thêm khách (cần thiết khi tạo HĐ)
    @FXML
    void showAddCustomerModal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/customer/AddCustomerModal.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Thêm khách mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.centerOnScreen();
            stage.showAndWait();
            loadData();
        } catch (Exception e) {
            showError("Lỗi mở modal: " + e.getMessage());
        }
    }

    private void showDetailModal(Customers customer) {
        if (customer == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/customer/CustomerDetailModal.fxml"));
            Parent root = loader.load();
            CustomerDetailController ctrl = loader.getController();
            ctrl.setCustomerData(customer);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Chi tiết khách hàng");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showEditModal(Customers customer) {
        if (customer == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/customer/CustomerEditModal.fxml"));
            Parent root = loader.load();
            CustomerEditController ctrl = loader.getController();
            ctrl.setCustomerData(customer);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Chỉnh sửa khách hàng");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
            loadData();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void loadDataToTable(ActionEvent event) {
        if (txtSearch != null) txtSearch.clear();
        if (cbFilter != null) cbFilter.setValue("Tất cả");
        loadData();
    }

    // ── Helpers ───────────────────────────────────────────
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Lỗi"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Thông báo"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}