package com.example.rentalcar.controller.vehicle;

import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.models.Vehicles;
import com.example.rentalcar.utils.AppSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class VehicleController {

    @FXML private TilePane         vehicleContainer;
    @FXML private TextField        txtSearch;
    @FXML private ComboBox<String> cbBrand;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button           btnAddNewVehicle;
    @FXML private Label            lblVehicleCount;

    private final VehicleBLL vehicleBLL = new VehicleBLL();

    @FXML
    public void initialize() {
        // ComboBox hãng xe – lấy động từ dữ liệu thực
        cbBrand.getItems().addAll("Tất cả", "Honda", "Yamaha", "SYM", "Suzuki");
        cbBrand.setValue("Tất cả");

        // ComboBox trạng thái – bao gồm cả Ngừng hoạt động
        cbStatus.getItems().addAll(
                "Tất cả",
                "Sẵn sàng",
                "Đang thuê",
                "Bảo dưỡng",
                "Đặt trước",
                "Ngừng hoạt động"
        );
        cbStatus.setValue("Tất cả");

        // Phân quyền: Staff không được thêm xe mới
        if (btnAddNewVehicle != null) {
            btnAddNewVehicle.setVisible(AppSession.isAdmin());
            btnAddNewVehicle.setManaged(AppSession.isAdmin());
        }

        loadVehicles();
    }

    // ── Load toàn bộ xe (kể cả INACTIVE) cho màn quản lý ──────
    public void loadVehicles() {
        // Dùng getAllVehiclesIncludeInactive() để Admin thấy xe đã xóa mềm
        List<Vehicles> list = vehicleBLL.getAllVehiclesIncludeInactive();
        renderCards(list);
    }

    // ── Render danh sách xe thành card ───────────────────────────
    private void renderCards(List<Vehicles> list) {
        vehicleContainer.getChildren().clear();

        if (list == null || list.isEmpty()) {
            Label empty = new Label("😔  Không tìm thấy xe nào phù hợp.");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-padding: 30;");
            vehicleContainer.getChildren().add(empty);
            updateCount(0);
            return;
        }

        for (Vehicles v : list) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/views/vehicle/VehicleCard.fxml"));
                Parent card = loader.load();

                VehicleCardController ctrl = loader.getController();
                ctrl.setData(v);
                ctrl.setOnRefresh(this::loadVehicles);

                vehicleContainer.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("Lỗi render thẻ xe [" + v.getCode_vehicle() + "]: " + e.getMessage());
                e.printStackTrace();
            }
        }

        updateCount(list.size());
    }

    // ── Tìm kiếm & lọc ───────────────────────────────────────────
    @FXML
    void handleSearch() {
        String keyword = txtSearch != null ? txtSearch.getText().trim()  : "";
        String brand   = cbBrand  != null ? cbBrand.getValue()           : "Tất cả";
        String status  = cbStatus != null ? cbStatus.getValue()          : "Tất cả";

        // searchVehicles dùng findAllIncludeInactive nên INACTIVE cũng được tìm thấy
        List<Vehicles> filtered = vehicleBLL.searchVehicles(keyword, brand, status);
        renderCards(filtered);
    }

    // ── Làm mới – reset filter và load lại ───────────────────────
    @FXML
    void handleRefresh() {
        if (txtSearch != null) txtSearch.clear();
        if (cbBrand   != null) cbBrand.setValue("Tất cả");
        if (cbStatus  != null) cbStatus.setValue("Tất cả");
        loadVehicles();
    }

    // ── Thêm xe mới (Admin only) ──────────────────────────────────
    @FXML
    void handleAddNewVehicle() {
        if (!AppSession.isAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/vehicle/AddVehicleView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Thêm phương tiện mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();
            loadVehicles();
        } catch (Exception e) {
            System.err.println("Lỗi mở form thêm xe: " + e.getMessage());
            e.printStackTrace();
            showError("Không thể mở form thêm xe: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────
    private void updateCount(int count) {
        if (lblVehicleCount != null)
            lblVehicleCount.setText(count + " xe");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}