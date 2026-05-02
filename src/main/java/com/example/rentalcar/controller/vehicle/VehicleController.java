package com.example.rentalcar.controller.vehicle;

import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.models.Vehicles;
import com.example.rentalcar.utils.AppSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class VehicleController {

    @FXML private TilePane         vehicleContainer;
    @FXML private TextField        txtSearch;
    @FXML private ComboBox<String> cbBrand, cbStatus;
    @FXML private Button           btnAddNewVehicle;  // fx:id trong VehicleManagement.fxml

    private final VehicleBLL vehicleBLL = new VehicleBLL();

    @FXML
    public void initialize() {
        cbBrand.getItems().addAll("Tất cả", "Honda", "Yamaha", "SYM");
        cbStatus.getItems().addAll("Tất cả", "AVAILABLE", "RENTED", "MAINTENANCE");

        // ── Phân quyền: Staff không được thêm xe mới ──────────
        if (btnAddNewVehicle != null) {
            btnAddNewVehicle.setVisible(AppSession.isAdmin());
            btnAddNewVehicle.setManaged(AppSession.isAdmin());
        }

        loadVehicles();
    }

    public void loadVehicles() {
        vehicleContainer.getChildren().clear();

        List<Vehicles> list = vehicleBLL.getAllVehicles();
        if (list == null) return;

        for (Vehicles v : list) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/views/vehicle/VehicleCard.fxml"));
                Parent card = loader.load();

                VehicleCardController ctrl = loader.getController();
                ctrl.setData(v);
                ctrl.setOnRefresh(this::loadVehicles);  // callback reload sau khi xóa

                vehicleContainer.getChildren().add(card);
            } catch (Exception e) {
                System.out.println("Lỗi render thẻ xe: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleAddNewVehicle() {
        if (!AppSession.isAdmin()) return;  // guard thêm
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
            loadVehicles(); // reload sau khi thêm
        } catch (Exception e) {
            System.err.println("Lỗi mở form thêm xe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String brand   = cbBrand.getValue();
        String status  = cbStatus.getValue();

        vehicleContainer.getChildren().clear();

        List<Vehicles> filtered = vehicleBLL.searchVehicles(keyword, brand, status);
        for (Vehicles v : filtered) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/views/vehicle/VehicleCard.fxml"));
                Parent card = loader.load();
                VehicleCardController ctrl = loader.getController();
                ctrl.setData(v);
                ctrl.setOnRefresh(this::loadVehicles);
                vehicleContainer.getChildren().add(card);
            } catch (Exception e) {
                System.out.println("Lỗi render card: " + e.getMessage());
            }
        }
    }
}