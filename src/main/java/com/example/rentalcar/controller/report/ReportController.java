package com.example.rentalcar.controller.report;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.time.LocalDate;

public class ReportController {
    @FXML private ComboBox<String> cbTimeFilter;
    @FXML private DatePicker dpDate;
    @FXML private Label lblTotalRevenue, lblTotalRentals, lblNewCustomers;
    @FXML private BarChart<String, Number> revenueChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private TableView<Object> tvTopVehicles; // Tạm thời để Object để demo
    @FXML private TableColumn<Object, String> colName;
    @FXML private TableColumn<Object, Integer> colTrips;
    @FXML private TableColumn<Object, Double> colIncome;

    @FXML
    public void initialize() {
        // Cài đặt các tùy chọn Tuần/Tháng/Năm
        cbTimeFilter.getItems().addAll("Theo Ngày", "Theo Tuần", "Theo Tháng", "Theo Năm");
        cbTimeFilter.setValue("Theo Tháng"); // Gắn mặc định là Tháng
        dpDate.setValue(LocalDate.now());    // Gắn mặc định là ngày hôm nay

        // Vẽ biểu đồ ảo ban đầu
        loadMockData();
    }

    @FXML
    void handleGenerateReport() {
        String filterType = cbTimeFilter.getValue();
        LocalDate selectedDate = dpDate.getValue();

        System.out.println("Đang lọc dữ liệu: " + filterType + " - Mốc thời gian: " + selectedDate);

        // TODO: Viết hàm gọi Database ở đây. Tùy vào filterType mà viết câu SELECT phù hợp.
        // Ví dụ: List<Data> list = reportBLL.getRevenue(filterType, selectedDate);
    }

    // Hàm tạo dữ liệu giả để xem thiết kế
    private void loadMockData() {
        revenueChart.getData().clear(); // Xóa dữ liệu cũ

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu (VNĐ)");

        // Thêm các cột biểu đồ
        series.getData().add(new XYChart.Data<>("Tuần 1", 15000000));
        series.getData().add(new XYChart.Data<>("Tuần 2", 22000000));
        series.getData().add(new XYChart.Data<>("Tuần 3", 18500000));
        series.getData().add(new XYChart.Data<>("Tuần 4", 31000000));

        revenueChart.getData().add(series);

        // Gán số cho thẻ KPI
        lblTotalRevenue.setText("86,500,000 đ");
        lblTotalRentals.setText("142");
        lblNewCustomers.setText("35");
    }
}