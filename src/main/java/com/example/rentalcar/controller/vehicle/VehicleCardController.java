package com.example.rentalcar.controller.vehicle;

import com.example.rentalcar.models.StatusVehicle;
import com.example.rentalcar.models.Vehicles;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

import javafx.scene.image.ImageView;
import java.text.NumberFormat;
import java.util.Locale;

public class VehicleCardController {
    @FXML private ImageView imageVehicle;
    @FXML private Label lblPlate, lblStatus, lblName, lblPrice;

    private Vehicles curentVehicle;
    private final NumberFormat formatVND = NumberFormat.getInstance(new Locale("vi", "VN"));

    public void setData(Vehicles vehicles)
    {
        this.curentVehicle = vehicles;
        if (vehicles == null)
            return;

        lblPlate.setText(vehicles.getCode_vehicle());
        lblName.setText(vehicles.getBrand() + " " + vehicles.getModel());
        lblPrice.setText(formatVND.format(vehicles.getPrice_day()) + " đ");

        StatusVehicle statusVehicle = vehicles.getStatus();
        lblStatus.setText(statusVehicle != null ? statusVehicle.name() : "UNKNOWN");

        lblStatus.getStyleClass().removeAll("status-active", "status-locked", "badge-available", "badge-rented", "badge-maintenance");
        if (statusVehicle == StatusVehicle.AVAILABLE)
            lblStatus.getStyleClass().add("badge-available");
        else if (statusVehicle == StatusVehicle.RENTED)
            lblStatus.getStyleClass().add("badge-rented");
        else if (statusVehicle == StatusVehicle.MAINTENANCE)
            lblStatus.getStyleClass().add("badge-maintenance");

        //Load ảnh
        try
        {
            String path = vehicles.getImage_url();
            if (path == null || path.isEmpty())
                path = "/image/dashboardform/card-moto.png";  //Ảnh mặc định
            Image image = new Image(getClass().getResourceAsStream(path));
            imageVehicle.setImage(image);
        }
        catch (Exception e)
        {
            System.out.println("Lỗi load ảnh cho xe: " + vehicles.getCode_vehicle());
        }
    }

    @FXML
    void handleEdit()
    {
        System.out.println("Edit xe");
    }

    @FXML
    void handleDelete()
    {
        System.out.printf("Yêu cầu xóa xe");
    }
}
