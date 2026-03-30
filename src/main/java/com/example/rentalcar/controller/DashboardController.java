package com.example.rentalcar.controller;

import com.example.rentalcar.dao.ContractDAO;
import com.example.rentalcar.dao.VehicleDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.io.IOException;

public class DashboardController {
    @FXML private Label label_status;
    @FXML private Label label_status_available;
    private VehicleDAO vehicleDAO = new VehicleDAO();

    @FXML private Label label_today_revenue;
    private ContractDAO contractDAO = new ContractDAO();

    @FXML
    public void initialize() {
        loadVehicleStatus();
        loadContractStatus();
    }

    private void loadVehicleStatus()
    {
        try {
            int total = vehicleDAO.getTotalActiveCars();
            int rented = vehicleDAO.getRentedCars();
            int available = vehicleDAO.getAvailableCars();

            if(label_status != null)
                label_status.setText(rented + "/" + total);
            if(label_status_available != null)
                label_status_available.setText("✅ " + available + " xe đang sẵn sàng");
        }
        catch (Exception e) {
            System.err.println("Lỗi nạp thống kê xe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadContractStatus() {
        try {
            double total = contractDAO.getTodayRevenue();
            if (label_today_revenue != null) {
                label_today_revenue.setText(String.format("%,.0f đ", total));
            }
        }
        catch (Exception e) {
            System.err.println("Lỗi nạp thống kê xe: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void showCreateContractModal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/create_contract/MainLayout.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Hệ thống - Tạo hợp đồng thuê xe mới");

            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setResizable(false);

            stage.show();

        } catch (IOException e) {
            System.err.println("Lỗi: Không tìm thấy file MainLayout.fxml! Kiểm tra lại folder create_contract nha.");
            e.printStackTrace();
        }
    }

}
