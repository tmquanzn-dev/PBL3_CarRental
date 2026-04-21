package com.example.rentalcar.controller.report;

import com.example.rentalcar.bll.ReportBLL;
import com.example.rentalcar.models.StaffReportRow;
import com.example.rentalcar.models.VehicleReportRow;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ReportController implements Initializable {

    // === KPI CARDS ===
    @FXML private Label lblYearRevenue, lblRevenueGrowth;
    @FXML private Label lblYearContracts, lblContractSub;
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblAvgMonthly;

    // === YEAR NAV ===
    @FXML private Label lblYear;

    // === BAR CHART ===
    @FXML private BarChart<String, Number> barChartRevenue;
    @FXML private CategoryAxis             xAxis;
    @FXML private NumberAxis               yAxis;
    @FXML private Label                    lblChartSubtitle;
    @FXML private Label                    lblPeakMonth;

    // === PIE CHART ===
    @FXML private PieChart pieChartStatus;

    // === TOP VEHICLES TABLE ===
    @FXML private TableView<VehicleReportRow>           tableTopVehicles;
    @FXML private TableColumn<VehicleReportRow, String> colVRank;
    @FXML private TableColumn<VehicleReportRow, String> colVName;
    @FXML private TableColumn<VehicleReportRow, String> colVPlate;
    @FXML private TableColumn<VehicleReportRow, String> colVCount;
    @FXML private TableColumn<VehicleReportRow, String> colVRevenue;

    // === TOP STAFF TABLE ===
    @FXML private TableView<StaffReportRow>           tableTopStaff;
    @FXML private TableColumn<StaffReportRow, String> colSRank;
    @FXML private TableColumn<StaffReportRow, String> colSName;
    @FXML private TableColumn<StaffReportRow, String> colSContracts;
    @FXML private TableColumn<StaffReportRow, String> colSRevenue;

    private final ReportBLL reportBLL = new ReportBLL();
    private int selectedYear = LocalDate.now().getYear();

    private static final String[] MONTH_LABELS = {
            "T1","T2","T3","T4","T5","T6",
            "T7","T8","T9","T10","T11","T12"
    };

    // ============================================================
    // INITIALIZE
    // ============================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBarChart();
        setupTableColumns();
        loadAllData();
    }

    // ============================================================
    // YEAR NAVIGATION
    // ============================================================
    @FXML void handlePrevYear(ActionEvent e) { selectedYear--; loadAllData(); }
    @FXML void handleNextYear(ActionEvent e) { selectedYear++; loadAllData(); }

    // ============================================================
    // EXPORT (placeholder)
    // ============================================================
    @FXML
    void handleExport(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Xuất báo cáo");
        alert.setHeaderText(null);
        alert.setContentText("Tính năng xuất báo cáo PDF/Excel đang được phát triển.");
        alert.showAndWait();
    }

    // ============================================================
    // SETUP BAR CHART (cấu hình ban đầu — không dùng LinearGradient)
    // ============================================================
    private void setupBarChart() {
        // Tắt animation để tránh lỗi render
        barChartRevenue.setAnimated(false);
        barChartRevenue.setLegendVisible(false);

        // Style trục — KHÔNG dùng LinearGradient cho chart-bar
        xAxis.setLabel("");
        yAxis.setLabel("(triệu đồng)");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number value) {
                if (value.doubleValue() == 0) return "0";
                return String.format("%.0f", value.doubleValue());
            }
        });
    }

    // ============================================================
    // LOAD ALL DATA
    // ============================================================
    private void loadAllData() {
        lblYear.setText(String.valueOf(selectedYear));
        loadKpiCards();
        loadBarChart();
        loadPieChart();
        loadTopVehicles();
        loadTopStaff();
    }

    // ------------------------------------------------------------
    // KPI CARDS
    // ------------------------------------------------------------
    private void loadKpiCards() {
        try {
            double revenue = reportBLL.getTotalRevenue(selectedYear);
            lblYearRevenue.setText(ReportBLL.formatMoneySmart(revenue));

            double growth = reportBLL.getRevenueGrowthPercent(selectedYear);
            if (growth > 0) {
                lblRevenueGrowth.setText(String.format("↗ +%.1f%% so với năm %d", growth, selectedYear - 1));
            } else if (growth < 0) {
                lblRevenueGrowth.setText(String.format("↘ %.1f%% so với năm %d", growth, selectedYear - 1));
            } else {
                lblRevenueGrowth.setText("Không có dữ liệu năm trước");
            }

            int contracts = reportBLL.getTotalContracts(selectedYear);
            lblYearContracts.setText(String.valueOf(contracts));
            lblContractSub.setText("đã hoàn thành trong năm " + selectedYear);

            lblTotalCustomers.setText(String.valueOf(reportBLL.getTotalCustomers(selectedYear)));
            lblAvgMonthly.setText(ReportBLL.formatMoneySmart(reportBLL.getAvgMonthlyRevenue(selectedYear)));

        } catch (Exception e) {
            System.err.println("LỖI loadKpiCards: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // BAR CHART — dùng màu SOLID, KHÔNG dùng LinearGradient
    // ------------------------------------------------------------
    private void loadBarChart() {
        try {
            barChartRevenue.getData().clear();

            Map<Integer, Double> monthlyData = reportBLL.getMonthlyRevenue(selectedYear);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu");

            double peak      = 0;
            int    peakMonth = 1;
            double totalRev  = 0;

            for (int m = 1; m <= 12; m++) {
                double rev        = monthlyData.getOrDefault(m, 0.0);
                double revMillion = rev / 1_000_000.0;
                series.getData().add(new XYChart.Data<>(MONTH_LABELS[m - 1], revMillion));
                totalRev += rev;
                if (rev > peak) { peak = rev; peakMonth = m; }
            }

            barChartRevenue.getData().add(series);

            // Subtitle
            lblChartSubtitle.setText("Tổng: " + ReportBLL.formatMoneySmart(totalRev)
                    + "  •  Đơn vị: triệu đồng");

            if (peak > 0) {
                lblPeakMonth.setText("🏆 Đỉnh: " + MONTH_LABELS[peakMonth - 1]
                        + " (" + ReportBLL.formatMoneySmart(peak) + ")");
                lblPeakMonth.setVisible(true);
            } else {
                lblPeakMonth.setText("Chưa có dữ liệu");
                lblPeakMonth.setVisible(true);
            }

            // *** FIX: Chỉ dùng màu SOLID — không dùng LinearGradient ***
            // LinearGradient gây ClassCastException trên JavaFX 21 cho chart-bar
            Platform.runLater(() -> {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle(
                                "-fx-bar-fill: #146dff;" +          // solid color
                                        "-fx-background-radius: 4 4 0 0;"
                        );
                        // Tooltip khi hover
                        double val = monthlyData.getOrDefault(
                                java.util.Arrays.asList(MONTH_LABELS).indexOf(data.getXValue()) + 1, 0.0);
                        Tooltip tip = new Tooltip(data.getXValue() + ": "
                                + ReportBLL.formatMoneyFull(val));
                        tip.setStyle("-fx-font-size: 13px;");
                        Tooltip.install(data.getNode(), tip);
                    }
                }
            });

        } catch (Exception e) {
            System.err.println("LỖI loadBarChart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------
    // PIE CHART — màu solid, không LinearGradient
    // ------------------------------------------------------------
    private void loadPieChart() {
        try {
            pieChartStatus.getData().clear();
            pieChartStatus.setAnimated(false);

            Map<String, Integer> dist = reportBLL.getContractStatusDistribution(selectedYear);

            if (dist.isEmpty()) {
                pieChartStatus.getData().add(new PieChart.Data("Chưa có dữ liệu", 1));
                return;
            }

            Map<String, String> displayNames = Map.of(
                    "DANG_THUE",  "Đang thuê",
                    "HOAN_THANH", "Hoàn thành",
                    "QUA_HAN",    "Quá hạn",
                    "DA_HUY",     "Đã hủy"
            );

            dist.forEach((status, count) -> {
                String label = displayNames.getOrDefault(status, status) + " (" + count + ")";
                pieChartStatus.getData().add(new PieChart.Data(label, count));
            });

            // Màu solid cho từng phần — không dùng LinearGradient
            String[] colors = {"#3b82f6", "#10b981", "#f59e0b", "#ef4444"};
            Platform.runLater(() -> {
                int i = 0;
                for (PieChart.Data d : pieChartStatus.getData()) {
                    if (d.getNode() != null)
                        d.getNode().setStyle("-fx-pie-color: " + colors[i % colors.length] + ";");
                    i++;
                }
            });

        } catch (Exception e) {
            System.err.println("LỖI loadPieChart: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // TOP VEHICLES / TOP STAFF
    // ------------------------------------------------------------
    private void loadTopVehicles() {
        try {
            List<VehicleReportRow> list = reportBLL.getTopVehicles(selectedYear);
            tableTopVehicles.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            System.err.println("LỖI loadTopVehicles: " + e.getMessage());
        }
    }

    private void loadTopStaff() {
        try {
            List<StaffReportRow> list = reportBLL.getTopStaff(selectedYear);
            tableTopStaff.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            System.err.println("LỖI loadTopStaff: " + e.getMessage());
        }
    }

    // ============================================================
    // SETUP TABLE COLUMNS
    // ============================================================
    private void setupTableColumns() {

        // --- TOP VEHICLES ---
        colVRank.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getRank())));
        colVRank.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setPrefSize(28, 28);
                badge.setAlignment(Pos.CENTER);
                badge.setStyle("-fx-background-radius: 50%; -fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" + rankBadgeStyle(Integer.parseInt(item)));
                HBox box = new HBox(badge);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        colVName.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getVehicleName()));
        colVName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size:13px;");
                setGraphic(lbl);
            }
        });

        colVPlate.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPlateNumber()));
        colVPlate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;" +
                        "-fx-padding: 3 8; -fx-background-radius: 6; -fx-font-weight: bold;");
                setGraphic(lbl);
            }
        });

        colVCount.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getRentalCount() + " lượt"));
        colVCount.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8;" +
                        "-fx-padding: 3 10; -fx-background-radius: 10; -fx-font-weight: bold;");
                setGraphic(lbl);
            }
        });

        colVRevenue.setCellValueFactory(cell ->
                new SimpleStringProperty(ReportBLL.formatMoneyFull(cell.getValue().getRevenue())));
        colVRevenue.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #e11d48; -fx-font-size:13px;");
                setGraphic(lbl);
            }
        });

        tableTopVehicles.setFixedCellSize(52);

        // --- TOP STAFF ---
        colSRank.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getRank())));
        colSRank.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setPrefSize(28, 28);
                badge.setAlignment(Pos.CENTER);
                badge.setStyle("-fx-background-radius: 50%; -fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" + rankBadgeStyle(Integer.parseInt(item)));
                HBox box = new HBox(badge);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        colSName.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getFullName()));
        colSName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Circle avatar = new Circle(16, Color.web("#4f46e5"));
                Text initText = new Text(item.isEmpty() ? "?" : item.substring(0, 1).toUpperCase());
                initText.setFill(Color.WHITE);
                initText.setStyle("-fx-font-weight: bold;");
                StackPane sp = new StackPane(avatar, initText);
                Label lbl = new Label(item);
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size:13px;");
                HBox box = new HBox(10, sp, lbl);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        colSContracts.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getContractCount() + " HĐ"));
        colSContracts.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d;" +
                        "-fx-padding: 3 10; -fx-background-radius: 10; -fx-font-weight: bold;");
                setGraphic(lbl);
            }
        });

        colSRevenue.setCellValueFactory(cell ->
                new SimpleStringProperty(ReportBLL.formatMoneyFull(cell.getValue().getRevenue())));
        colSRevenue.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label lbl = new Label(item);
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #e11d48; -fx-font-size:13px;");
                setGraphic(lbl);
            }
        });

        tableTopStaff.setFixedCellSize(52);
    }

    // ============================================================
    // HELPER — badge màu theo hạng
    // ============================================================
    private String rankBadgeStyle(int rank) {
        return switch (rank) {
            case 1 -> "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;";
            case 2 -> "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;";
            case 3 -> "-fx-background-color: #fce7f3; -fx-text-fill: #be185d;";
            default -> "-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8;";
        };
    }
}