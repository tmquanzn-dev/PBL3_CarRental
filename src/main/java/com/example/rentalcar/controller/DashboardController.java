package com.example.rentalcar.controller;

import com.example.rentalcar.dao.ContractDAO;
import com.example.rentalcar.dao.VehicleDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

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

}
