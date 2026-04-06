package com.example.rentalcar.controller.customer;

import com.example.rentalcar.bll.CustomerBLL;
import com.example.rentalcar.models.Customers;
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
import javafx.scene.text.Text;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {

    // Khai báo 3 Label của 3 card thống kê
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblNewCustomers;
    @FXML private Label lblBlacklist;

    // Khai báo các cột và bảng dữ liệu
    @FXML private TableView<Customers> tableCustomers;
    @FXML private TableColumn<Customers, String> colCccd;
    @FXML private TableColumn<Customers, String> colName;
    @FXML private TableColumn<Customers, String> colPhone;
    @FXML private TableColumn<Customers, Integer> colRentalCount;
    @FXML private TableColumn<Customers, Boolean> colStatus;
    @FXML private TableColumn<Customers, Void> colAction;


    // Gọi BLL
    private final CustomerBLL customerBLL = new CustomerBLL();
    private ObservableList<Customers> customerList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadData();
        tableCustomers.setFixedCellSize(60.0);
    }

    private void setupTableColumns()
    {
        colCccd.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCccd())
        );
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        colName.setCellValueFactory(new PropertyValueFactory<>("full_name"));
        colName.setCellFactory(column -> new TableCell<Customers, String>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);

                if (empty || name == null || name.isBlank()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Vẽ Avatar tròn
                    Circle avatar = new Circle(15, Color.web("#478dff"));
                    Text initial = new Text(name.substring(0, 1).toUpperCase());
                    initial.setFill(Color.WHITE);

                    StackPane stackAvatar = new StackPane(avatar, initial);
                    Label lblName = new Label(name);
                    HBox box = new HBox(15, stackAvatar, lblName);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        colRentalCount.setCellValueFactory(new PropertyValueFactory<>("rental_count"));
        colRentalCount.setCellFactory(column -> new TableCell<Customers, Integer>()
        {
            @Override
            protected void updateItem(Integer count, boolean empty)
            {
                super.updateItem(count, empty);
                if (empty || count == null)
                    setGraphic(null);
                else
                {
                    Label badge = new Label(count + " lần");
                    badge.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca; " +
                            "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: bold;");
                    HBox box = new HBox(badge);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }

            }
        });

        colStatus.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isIs_blacklist())
        );
        colStatus.setCellFactory(column -> new TableCell<Customers, Boolean>() {
            @Override
            protected void updateItem(Boolean isBlacklist, boolean empty) {
                super.updateItem(isBlacklist, empty);
                if (empty || isBlacklist == null) {
                    setGraphic(null);
                } else {
                    Label pill = new Label(isBlacklist ? "Blacklist" : "Bình thường");

                    // Xóa hết style cũ, dùng class từ CSS bạn gửi
                    pill.getStyleClass().add("status-pill");

                    if (isBlacklist) {
                        pill.getStyleClass().add("status-yellow"); // Hoặc status-gray tùy bạn
                    } else {
                        pill.getStyleClass().add("status-green");
                    }

                    HBox box = new HBox(pill);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });

        colAction.setCellFactory(column -> new TableCell<Customers, Void>() {
            private final Button btnView = new Button();
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox pane = new HBox(12, btnView, btnEdit, btnDelete);

            {
                pane.setAlignment(Pos.CENTER_LEFT);
                btnView.getStyleClass().add("btn-action");
                btnEdit.getStyleClass().add("btn-action");
                btnDelete.getStyleClass().add("btn-action");

                loadIcon(btnView, "/image/dashboardform/view.png");
                loadIcon(btnEdit, "/image/dashboardform/edit.png");
                loadIcon(btnDelete, "/image/dashboardform/delete.png");
            }

            private void loadIcon(Button btn, String path) {
                try {
                    URL url = getClass().getResource(path);
                    if (url != null) {
                        ImageView iv = new ImageView(new Image(url.toExternalForm()));
                        iv.setFitHeight(18); iv.setFitWidth(18);
                        btn.setGraphic(iv);
                    }
                } catch (Exception ex) { }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadData() {
        try {
            // 1. Lấy danh sách khách hàng từ BLL
            List<Customers> list = customerBLL.getAllCustomers();

            if (list != null) {
                customerList = FXCollections.observableArrayList(list);
                tableCustomers.setItems(customerList);
                int tongKhach = list.size();
                lblTotalCustomers.setText(String.valueOf(tongKhach));
                int soLuongBlacklist = 0;
                for (int i = 0; i < list.size(); i++) {
                    Customers c = list.get(i);
                    if (c.isIs_blacklist() == true)
                        soLuongBlacklist = soLuongBlacklist + 1;

                }
                lblBlacklist.setText(String.valueOf(soLuongBlacklist));
                lblNewCustomers.setText("0");
            }
        } catch (Exception e) {
            System.out.println("Lỗi rồi: " + e.getMessage());
        }
    }

    // Hàm mở popup thêm khách hàng mới
    @FXML
    void showAddCustomerModal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/customer/AddCustomerModal.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.setScene(new Scene(root));

            modalStage.setTitle("Thêm khách mới");

            modalStage.initModality(Modality.APPLICATION_MODAL);
//            modalStage.initStyle(StageStyle.UNDECORATED);
            modalStage.centerOnScreen();
            modalStage.showAndWait();

            // loadData();

        } catch (Exception e) {
            System.err.println("Lỗi khi mở modal Thêm Khách Hàng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Hàm xử lý tìm kiếm
    @FXML
    void handleSearch(ActionEvent event) {
        System.out.println("Đang tìm kiếm khách hàng...");
    }

    // Hàm làm mới bảng dữ liệu
    @FXML
    void loadDataToTable(ActionEvent event) {
        System.out.println("Đang load lại dữ liệu vào bảng...");
    }
}