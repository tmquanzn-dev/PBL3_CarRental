package com.example.rentalcar.controller.vehicle;

import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.models.Vehicles;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;


public class VehicleController {
    @FXML private TilePane vehicleContainer;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbBrand, cbStatus;

    private final VehicleBLL vehicleBLL = new VehicleBLL();

    @FXML
    public void initialize()
    {
        cbBrand.getItems().addAll("Tất cả", "Honda", "Yamaha", "SYM");
        cbStatus.getItems().addAll("Tất cả", "AVAILABLE", "RENTED", "MAINTENANCE");
        loadVehicles();
    }

    public void loadVehicles() {
        vehicleContainer.getChildren().clear();

        List<Vehicles> li = vehicleBLL.getAllVehicles();
        if (li != null) {
            for (Vehicles v : li) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/vehicle/VehicleCard.fxml"));
                    Parent card = loader.load();

                    VehicleCardController cardController = loader.getController();
                    cardController.setData(v);

                    vehicleContainer.getChildren().add(card);
                } catch (Exception e) {
                    System.out.println("Lỗi render thẻ xe: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    void handleAddNewVehicle() {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/vehicle/AddVehicleView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Thêm phương tiện mới");
            stage.setScene(new Scene(root));

            //Thao tác trên này xong mới được bấm ra ngoài
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();

            stage.showAndWait();
            loadVehicles(); // tự động load lại card khi thêm xe mới
        } catch (Exception e) {
            System.err.println("Lỗi mở form thêm xe: "+ e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleSearch() {
        // Logic lọc dữ liệu từ SearchField
        System.out.println("Đang tìm kiếm: " + txtSearch.getText());
    }

}
