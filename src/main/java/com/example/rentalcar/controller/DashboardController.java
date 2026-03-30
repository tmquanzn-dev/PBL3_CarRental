package com.example.rentalcar.controller;

import com.example.rentalcar.dao.ContractDAO;
import com.example.rentalcar.dao.VehicleDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    // Giao diện Thống kê Xe
    @FXML private Label label_status;
    @FXML private Label label_status_available;

    // Giao diện Thống kê Doanh thu
    @FXML private Label label_today_revenue;

    // Khởi tạo các DAO (Thủ kho) để lấy dữ liệu
    private VehicleDAO vehicleDAO;
    private ContractDAO contractDAO;

    // Hàm initialize() chạy tự động ngay khi giao diện Dashboard vừa load xong
    @FXML
    public void initialize() {
        // Khởi tạo DAO
        vehicleDAO = new VehicleDAO();
        contractDAO = new ContractDAO();

        // Gọi các hàm nạp dữ liệu lên màn hình
        loadVehicleStatus();
        loadContractStatus();
    }

    private void loadVehicleStatus()
    {
        try {
            int total = vehicleDAO.getTotalActiveCars();
            int rented = vehicleDAO.getRentedCars();
            int available = vehicleDAO.getAvailableCars();

            if (label_status != null) {
                // Hiển thị định dạng: "Số xe đang cho thuê / Tổng số xe" (VD: 5/20)
                label_status.setText(rented + "/" + total);
            }

            if (label_status_available != null) {
                label_status_available.setText("✅ " + available + " xe đang sẵn sàng");
            }
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
                // Format số tiền có dấu phẩy phân cách hàng nghìn (VD: 1,500,000 đ)
                label_today_revenue.setText(String.format("%,.0f đ", total));
            }
        }
        catch (Exception e) {
            // Anh đã fix lại dòng thông báo lỗi này cho chuẩn xác
            System.err.println("Lỗi nạp thống kê doanh thu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}