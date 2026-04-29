package com.example.rentalcar.controller.report;

import com.example.rentalcar.bll.ReportBLL;
import com.example.rentalcar.models.StaffReportRow;
import com.example.rentalcar.models.VehicleReportRow;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
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

    @FXML private Label lblYearRevenue, lblRevenueGrowth;
    @FXML private Label lblYearContracts, lblContractSub;
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblAvgMonthly;
    @FXML private Label lblYear;
    @FXML private BarChart<String, Number> barChartRevenue;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis   yAxis;
    @FXML private Label lblChartSubtitle;
    @FXML private Label lblPeakMonth;
    @FXML private PieChart pieChartStatus;

    @FXML private TableView<VehicleReportRow>           tableTopVehicles;
    @FXML private TableColumn<VehicleReportRow, Integer> colVRank;
    @FXML private TableColumn<VehicleReportRow, String> colVName;
    @FXML private TableColumn<VehicleReportRow, String> colVPlate;
    @FXML private TableColumn<VehicleReportRow, String> colVCount;
    @FXML private TableColumn<VehicleReportRow, Double> colVRevenue;

    @FXML private TableView<StaffReportRow>           tableTopStaff;
    @FXML private TableColumn<StaffReportRow, Integer> colSRank;
    @FXML private TableColumn<StaffReportRow, String> colSName;
    @FXML private TableColumn<StaffReportRow, String> colSContracts;
    @FXML private TableColumn<StaffReportRow, Double> colSRevenue;

    private final ReportBLL reportBLL = new ReportBLL();
    private int selectedYear = LocalDate.now().getYear();

    private static final String[] MONTH_LABELS = {
            "T1","T2","T3","T4","T5","T6",
            "T7","T8","T9","T10","T11","T12"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBarChart();
        setupTableColumns();
        setupRowFactories();   // ← force row height
        loadAllData();
    }

    // ============================================================
    // RowFactory: force mỗi row cao ít nhất 46px
    // ============================================================
    private void setupRowFactories() {
        tableTopVehicles.setRowFactory(tv -> {
            TableRow<VehicleReportRow> row = new TableRow<>();
            row.setMinHeight(46);
            row.setPrefHeight(46);
            return row;
        });
        tableTopStaff.setRowFactory(tv -> {
            TableRow<StaffReportRow> row = new TableRow<>();
            row.setMinHeight(46);
            row.setPrefHeight(46);
            return row;
        });
    }

    @FXML void handlePrevYear(ActionEvent e) { selectedYear--; loadAllData(); }
    @FXML void handleNextYear(ActionEvent e) { selectedYear++; loadAllData(); }

    @FXML
    void handleExport(ActionEvent e) {
        new Alert(Alert.AlertType.INFORMATION, "Tính năng đang phát triển.").showAndWait();
    }

    private void setupBarChart() {
        barChartRevenue.setAnimated(false);
        barChartRevenue.setLegendVisible(false);
        xAxis.setLabel("");
        yAxis.setLabel("(triệu đồng)");
    }

    private void loadAllData() {
        lblYear.setText(String.valueOf(selectedYear));
        loadKpiCards();
        loadBarChart();
        loadPieChart();
        loadTopVehicles();
        loadTopStaff();
    }

    // ============================================================
    // KPI
    // ============================================================
    private void loadKpiCards() {
        try {
            lblYearRevenue.setText(ReportBLL.formatMoneySmart(reportBLL.getTotalRevenue(selectedYear)));

            double g = reportBLL.getRevenueGrowthPercent(selectedYear);
            lblRevenueGrowth.setText(g > 0
                    ? String.format("↗ +%.1f%% so với năm %d", g, selectedYear - 1)
                    : g < 0
                    ? String.format("↘ %.1f%% so với năm %d", g, selectedYear - 1)
                    : "Không có dữ liệu năm trước");

            lblYearContracts.setText(String.valueOf(reportBLL.getTotalContracts(selectedYear)));
            lblContractSub.setText("đã hoàn thành trong năm " + selectedYear);
            lblTotalCustomers.setText(String.valueOf(reportBLL.getTotalCustomers(selectedYear)));
            lblAvgMonthly.setText(ReportBLL.formatMoneySmart(reportBLL.getAvgMonthlyRevenue(selectedYear)));
        } catch (Exception e) {
            System.err.println("[Report] KPI: " + e.getMessage());
        }
    }

    // ============================================================
    // BAR CHART
    // ============================================================
    private void loadBarChart() {
        try {
            barChartRevenue.getData().clear();
            Map<Integer, Double> data = reportBLL.getMonthlyRevenue(selectedYear);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu");

            double peak = 0, total = 0; int peakMonth = 1;
            for (int m = 1; m <= 12; m++) {
                double rev = data.getOrDefault(m, 0.0);
                series.getData().add(new XYChart.Data<>(MONTH_LABELS[m - 1], rev / 1_000_000.0));
                total += rev;
                if (rev > peak) { peak = rev; peakMonth = m; }
            }
            barChartRevenue.getData().add(series);
            lblChartSubtitle.setText("Tổng: " + ReportBLL.formatMoneySmart(total) + "  •  Đơn vị: triệu đồng");
            lblPeakMonth.setText(peak > 0
                    ? "🏆 Đỉnh: " + MONTH_LABELS[peakMonth - 1] + " (" + ReportBLL.formatMoneySmart(peak) + ")"
                    : "");
            lblPeakMonth.setVisible(peak > 0);

            Platform.runLater(() -> {
                for (XYChart.Data<String, Number> d : series.getData()) {
                    if (d.getNode() != null)
                        d.getNode().setStyle("-fx-bar-fill: #146dff; -fx-background-radius: 4 4 0 0;");
                }
            });
        } catch (Exception e) { System.err.println("[Report] BarChart: " + e.getMessage()); }
    }

    // ============================================================
    // PIE CHART
    // ============================================================
    private void loadPieChart() {
        try {
            pieChartStatus.getData().clear();
            pieChartStatus.setAnimated(false);
            Map<String, Integer> dist = reportBLL.getContractStatusDistribution(selectedYear);
            if (dist.isEmpty()) { pieChartStatus.getData().add(new PieChart.Data("Chưa có dữ liệu", 1)); return; }
            Map<String, String> names = Map.of(
                    "DANG_THUE","Đang thuê","DANG THUE","Đang thuê",
                    "HOAN_THANH","Hoàn thành","HOAN THANH","Hoàn thành",
                    "QUA_HAN","Quá hạn","QUA HAN","Quá hạn",
                    "DA_HUY","Đã hủy","DA HUY","Đã hủy");
            dist.forEach((s, c) -> pieChartStatus.getData()
                    .add(new PieChart.Data(names.getOrDefault(s, s) + " (" + c + ")", c)));
            String[] colors = {"#3b82f6","#10b981","#f59e0b","#ef4444"};
            Platform.runLater(() -> {
                int i = 0;
                for (PieChart.Data d : pieChartStatus.getData()) {
                    if (d.getNode() != null) d.getNode().setStyle("-fx-pie-color:" + colors[i % 4] + ";");
                    i++;
                }
            });
        } catch (Exception e) { System.err.println("[Report] PieChart: " + e.getMessage()); }
    }

    // ============================================================
    // LOAD TABLES
    // ============================================================
    private void loadTopVehicles() {
        try {
            List<VehicleReportRow> list = reportBLL.getTopVehicles(selectedYear);
            System.out.println("[Report] Top vehicles: " + list.size() + " dòng");
            tableTopVehicles.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadTopStaff() {
        try {
            List<StaffReportRow> list = reportBLL.getTopStaff(selectedYear);
            System.out.println("[Report] Top staff: " + list.size() + " dòng");
            tableTopStaff.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ============================================================
    // TABLE COLUMNS
    // Dùng setText() cho các cột text đơn giản thay vì setGraphic()
    // Chỉ dùng setGraphic() cho rank badge và staff avatar
    // ============================================================

    private void setupTableColumns() {
        // ── 1. BẢNG TOP XE ──────────────────────
        colVRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colVName.setCellValueFactory(new PropertyValueFactory<>("vehicleName"));
        colVPlate.setCellValueFactory(new PropertyValueFactory<>("plateNumber"));
        colVCount.setCellValueFactory(new PropertyValueFactory<>("rentalCount"));
        colVRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        // Vẽ Badge cho Rank (Nhận Integer)
        colVRank.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label b = new Label(String.valueOf(item));
                b.setPrefSize(30, 30); b.setAlignment(Pos.CENTER);
                b.setStyle("-fx-background-radius:50%; -fx-font-weight:bold;" + rankStyle(item));
                setGraphic(b);
            }
        });

        // Format tiền Doanh thu
        colVRevenue.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(ReportBLL.formatMoneyFull(item));
                setStyle("-fx-font-weight:bold; -fx-text-fill:#e11d48;");
            }
        });

        // ── 2. BẢNG TOP NHÂN VIÊN ──────────────────
        colSRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colSName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colSContracts.setCellValueFactory(new PropertyValueFactory<>("contractCount"));
        colSRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        // Avatar nhân viên
        colSName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Circle av = new Circle(14, Color.web("#4f46e5"));
                Text t = new Text(item.isEmpty() ? "?" : item.substring(0, 1).toUpperCase());
                t.setFill(Color.WHITE);
                StackPane sp = new StackPane(av, t);
                Label l = new Label(item);
                l.setStyle("-fx-font-weight:bold;");
                HBox box = new HBox(8, sp, l); box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });
    }

    private String rankStyle(int rank) {
        return switch (rank) {
            case 1 -> "-fx-background-color:#fef3c7;-fx-text-fill:#b45309;";
            case 2 -> "-fx-background-color:#f1f5f9;-fx-text-fill:#475569;";
            case 3 -> "-fx-background-color:#fce7f3;-fx-text-fill:#be185d;";
            default -> "-fx-background-color:#f1f5f9;-fx-text-fill:#94a3b8;";
        };
    }
}