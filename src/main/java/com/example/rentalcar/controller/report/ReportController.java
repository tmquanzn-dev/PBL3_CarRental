package com.example.rentalcar.controller.report;

import com.example.rentalcar.bll.ReportBLL;
import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.models.StaffReportRow;
import com.example.rentalcar.models.VehicleReportRow;
import com.example.rentalcar.utils.ReportPrinter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Window;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ReportController implements Initializable {

    // ── KPI Cards ─────────────────────────────────────────
    @FXML private Label lblYearRevenue, lblRevenueGrowth;
    @FXML private Label lblYearContracts, lblContractSub;
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblAvgMonthly;

    // ── Header ────────────────────────────────────────────
    @FXML private Label lblYear;
    @FXML private ComboBox<String> cbMonth;

    // ── Bar Chart ─────────────────────────────────────────
    @FXML private BarChart<String, Number> barChartRevenue;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis   yAxis;
    @FXML private Label lblChartSubtitle, lblPeakMonth;

    // ── Pie Chart ─────────────────────────────────────────
    @FXML private PieChart pieChartStatus;

    // ── Top 5 xe ──────────────────────────────────────────
    @FXML private VBox  vboxTopVehicles;
    @FXML private Label lblTopVehiclesPeriod;
    @FXML private Label lblNoVehicles;

    // ── Top 5 nhân viên ───────────────────────────────────
    @FXML private VBox  vboxTopStaff;
    @FXML private Label lblTopStaffPeriod;
    @FXML private Label lblNoStaff;

    // ── Trạng thái xe ─────────────────────────────────────
    @FXML private Label lblVehAvailable, lblVehRented, lblVehMaintenance, lblVehTotal;

    // ── Tổng kết tháng ────────────────────────────────────
    @FXML private Label lblCurrentMonthLabel;
    @FXML private Label lblMonthRevenue, lblMonthContracts, lblMonthCustomers;
    @FXML private Label lblPrevMonthRevenue, lblPrevMonthContracts, lblGrowthBadge;

    // ── BLL ───────────────────────────────────────────────
    private final ReportBLL  reportBLL  = new ReportBLL();
    private final VehicleBLL vehicleBLL = new VehicleBLL();

    private int selectedYear  = LocalDate.now().getYear();
    private int selectedMonth = LocalDate.now().getMonthValue(); // 0 = tất cả tháng

    private static final String[] MONTH_LABELS = {
            "T1","T2","T3","T4","T5","T6",
            "T7","T8","T9","T10","T11","T12"
    };

    private static final String[] MONTH_NAMES = {
            "Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
            "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"
    };

    // ─────────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMonthCombo();
        setupBarChart();
        // setupTableColumns() removed - using VBox rows instead
        loadAllData();
    }

    // ─────────────────────────────────────────────────────
    //  SETUP
    // ─────────────────────────────────────────────────────
    private void setupMonthCombo() {
        if (cbMonth == null) return;
        cbMonth.getItems().add("Cả năm");
        for (String mn : MONTH_NAMES) cbMonth.getItems().add(mn);
        // Mặc định chọn tháng hiện tại
        cbMonth.getSelectionModel().select(selectedMonth); // index = tháng (1-12)
        cbMonth.setOnAction(e -> {
            int idx = cbMonth.getSelectionModel().getSelectedIndex();
            selectedMonth = idx; // 0 = cả năm, 1..12 = tháng cụ thể
            refreshTopTables();
            refreshMonthSummary();
        });
    }

    private void setupBarChart() {
        barChartRevenue.setAnimated(false);
        barChartRevenue.setLegendVisible(false);
        xAxis.setLabel("");
        yAxis.setLabel("(tr.đ)");
    }

    // ─────────────────────────────────────────────────────
    //  ĐIỀU HƯỚNG NĂM
    // ─────────────────────────────────────────────────────
    @FXML void handlePrevYear(ActionEvent e) { selectedYear--; loadAllData(); }
    @FXML void handleNextYear(ActionEvent e) { selectedYear++; loadAllData(); }

    @FXML void handleRefresh(ActionEvent e) {
        selectedYear  = LocalDate.now().getYear();
        selectedMonth = LocalDate.now().getMonthValue();
        if (cbMonth != null) cbMonth.getSelectionModel().select(selectedMonth);
        loadAllData();
    }

    // ─────────────────────────────────────────────────────
    //  LOAD TẤT CẢ
    // ─────────────────────────────────────────────────────
    private void loadAllData() {
        if (lblYear != null) lblYear.setText(String.valueOf(selectedYear));
        loadKpiCards();
        loadBarChart();
        loadPieChart();
        loadVehicleStatus();
        refreshTopTables();
        refreshMonthSummary();
    }

    // ─────────────────────────────────────────────────────
    //  KPI CARDS
    // ─────────────────────────────────────────────────────
    private void loadKpiCards() {
        try {
            double totalRev = reportBLL.getTotalRevenue(selectedYear);
            setLabel(lblYearRevenue, ReportBLL.formatMoneySmart(totalRev));

            double growth = reportBLL.getRevenueGrowthPercent(selectedYear);
            setLabel(lblRevenueGrowth,
                    growth != 0
                            ? ReportBLL.formatPercent(growth) + " so với " + (selectedYear-1)
                            : "Không có dữ liệu năm trước");

            int contracts = reportBLL.getTotalContracts(selectedYear);
            setLabel(lblYearContracts, String.valueOf(contracts));
            setLabel(lblContractSub, "hợp đồng hoàn thành năm " + selectedYear);

            setLabel(lblTotalCustomers, String.valueOf(reportBLL.getTotalCustomers(selectedYear)));
            setLabel(lblAvgMonthly, ReportBLL.formatMoneySmart(reportBLL.getAvgMonthlyRevenue(selectedYear)));

        } catch (Exception ex) {
            System.err.println("[Report] KPI: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────
    //  BAR CHART
    // ─────────────────────────────────────────────────────
    private void loadBarChart() {
        try {
            barChartRevenue.getData().clear();
            Map<Integer, Double> data = reportBLL.getMonthlyRevenue(selectedYear);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu");

            double peak = 0, total = 0;
            int peakMonth = 1;
            for (int m = 1; m <= 12; m++) {
                double rev = data.getOrDefault(m, 0.0);
                series.getData().add(new XYChart.Data<>(MONTH_LABELS[m-1], rev / 1_000_000.0));
                total += rev;
                if (rev > peak) { peak = rev; peakMonth = m; }
            }
            barChartRevenue.getData().add(series);

            setLabel(lblChartSubtitle, "Tổng: " + ReportBLL.formatMoneySmart(total) + "  •  Đơn vị: triệu đồng");

            if (lblPeakMonth != null) {
                if (peak > 0) {
                    lblPeakMonth.setText("🏆 Đỉnh: " + MONTH_LABELS[peakMonth-1]
                            + " (" + ReportBLL.formatMoneySmart(peak) + ")");
                    lblPeakMonth.setVisible(true);
                } else {
                    lblPeakMonth.setVisible(false);
                }
            }

            // Tô màu thanh
            final int pkMonth = peakMonth;
            Platform.runLater(() -> {
                for (int i = 0; i < series.getData().size(); i++) {
                    XYChart.Data<String, Number> d = series.getData().get(i);
                    if (d.getNode() != null) {
                        String color = (i + 1 == pkMonth) ? "#ef4444" : "#3b82f6";
                        d.getNode().setStyle("-fx-bar-fill: " + color + "; -fx-background-radius: 4 4 0 0;");
                    }
                }
            });

        } catch (Exception ex) {
            System.err.println("[Report] BarChart: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────
    //  PIE CHART
    // ─────────────────────────────────────────────────────
    private void loadPieChart() {
        try {
            pieChartStatus.getData().clear();
            pieChartStatus.setAnimated(false);
            Map<String, Integer> dist = reportBLL.getContractStatusDistribution(selectedYear);
            if (dist.isEmpty()) {
                pieChartStatus.getData().add(new PieChart.Data("Chưa có dữ liệu", 1));
                return;
            }
            Map<String, String> nameMap = Map.of(
                    "DANG_THUE","Đang thuê", "DANG THUE","Đang thuê",
                    "HOAN_THANH","Hoàn thành","HOAN THANH","Hoàn thành",
                    "QUA_HAN","Quá hạn",   "QUA HAN","Quá hạn",
                    "DA_HUY","Đã hủy",     "DA HUY","Đã hủy");
            dist.forEach((s, c) -> pieChartStatus.getData()
                    .add(new PieChart.Data(nameMap.getOrDefault(s, s) + " (" + c + ")", c)));
            String[] colors = {"#3b82f6","#10b981","#f59e0b","#ef4444"};
            Platform.runLater(() -> {
                int i = 0;
                for (PieChart.Data d : pieChartStatus.getData()) {
                    if (d.getNode() != null)
                        d.getNode().setStyle("-fx-pie-color: " + colors[i % 4] + ";");
                    i++;
                }
            });
        } catch (Exception ex) {
            System.err.println("[Report] PieChart: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────
    //  TRẠNG THÁI XE
    // ─────────────────────────────────────────────────────
    private void loadVehicleStatus() {
        try {
            Map<String, Integer> veh = reportBLL.getVehicleStatusCount();
            setLabel(lblVehAvailable,   String.valueOf(veh.getOrDefault("AVAILABLE", 0)));
            setLabel(lblVehRented,      String.valueOf(veh.getOrDefault("RENTED", 0)));
            setLabel(lblVehMaintenance, String.valueOf(veh.getOrDefault("MAINTENANCE", 0)));
            setLabel(lblVehTotal,       String.valueOf(veh.getOrDefault("TOTAL", 0)));
        } catch (Exception ex) {
            System.err.println("[Report] VehicleStatus: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────
    //  TOP 5 (theo tháng hoặc năm) — dùng VBox rows, không TableView
    // ─────────────────────────────────────────────────────
    private void refreshTopTables() {
        try {
            boolean isMonthMode = selectedMonth > 0;
            String periodLabel = isMonthMode
                    ? MONTH_NAMES[selectedMonth - 1] + "/" + selectedYear
                    : "Năm " + selectedYear;

            setLabel(lblTopVehiclesPeriod, periodLabel);
            setLabel(lblTopStaffPeriod,    periodLabel);

            // Top xe
            List<VehicleReportRow> vList = isMonthMode
                    ? reportBLL.getTopVehiclesByMonth(selectedYear, selectedMonth)
                    : reportBLL.getTopVehiclesByYear(selectedYear);
            renderVehicleRows(vList);

            // Top nhân viên
            List<StaffReportRow> sList = isMonthMode
                    ? reportBLL.getTopStaffByMonth(selectedYear, selectedMonth)
                    : reportBLL.getTopStaffByYear(selectedYear);
            renderStaffRows(sList);

        } catch (Exception ex) {
            System.err.println("[Report] TopTables: " + ex.getMessage());
        }
    }

    private void renderVehicleRows(List<VehicleReportRow> list) {
        if (vboxTopVehicles == null) return;
        vboxTopVehicles.getChildren().clear();
        if (list == null || list.isEmpty()) {
            if (lblNoVehicles != null) { lblNoVehicles.setVisible(true); lblNoVehicles.setManaged(true); }
            return;
        }
        if (lblNoVehicles != null) { lblNoVehicles.setVisible(false); lblNoVehicles.setManaged(false); }
        for (VehicleReportRow row : list) {
            HBox hbox = new HBox();
            hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            hbox.setMinHeight(48); hbox.setPrefHeight(48);
            hbox.setStyle(row.getRank() % 2 == 0
                    ? "-fx-background-color:#f8fafc;-fx-padding:0 10;-fx-border-color:transparent transparent #f1f5f9 transparent;-fx-border-width:1;"
                    : "-fx-background-color:white;-fx-padding:0 10;-fx-border-color:transparent transparent #f1f5f9 transparent;-fx-border-width:1;");

            // Rank badge
            Label rank = new Label(String.valueOf(row.getRank()));
            rank.setPrefWidth(40); rank.setMinHeight(48);
            rank.setAlignment(javafx.geometry.Pos.CENTER);
            rank.setStyle(rankBadgeStyle(row.getRank()));

            // Tên xe
            Label name = new Label(row.getVehicleName());
            name.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
            HBox.setHgrow(name, javafx.scene.layout.Priority.ALWAYS);
            name.setMaxWidth(Double.MAX_VALUE);

            // Biển số
            Label plate = new Label(row.getPlateNumber());
            plate.setPrefWidth(95);
            plate.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#475569;" +
                    "-fx-padding:3 7;-fx-background-radius:6;-fx-font-size:11px;-fx-font-weight:bold;");

            // Lượt
            Label count = new Label(row.getRentalCount() + " lượt");
            count.setPrefWidth(55);
            count.setAlignment(javafx.geometry.Pos.CENTER);
            count.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#2563eb;");

            // Doanh thu
            Label rev = new Label(ReportBLL.formatMoneyFull(row.getRevenue()));
            rev.setPrefWidth(115);
            rev.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            rev.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#dc2626;");

            hbox.getChildren().addAll(rank, name, plate, count, rev);
            vboxTopVehicles.getChildren().add(hbox);
        }
    }

    private void renderStaffRows(List<StaffReportRow> list) {
        if (vboxTopStaff == null) return;
        vboxTopStaff.getChildren().clear();
        if (list == null || list.isEmpty()) {
            if (lblNoStaff != null) { lblNoStaff.setVisible(true); lblNoStaff.setManaged(true); }
            return;
        }
        if (lblNoStaff != null) { lblNoStaff.setVisible(false); lblNoStaff.setManaged(false); }
        for (StaffReportRow row : list) {
            HBox hbox = new HBox();
            hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            hbox.setMinHeight(48); hbox.setPrefHeight(48);
            hbox.setStyle(row.getRank() % 2 == 0
                    ? "-fx-background-color:#f8fafc;-fx-padding:0 10;-fx-border-color:transparent transparent #f1f5f9 transparent;-fx-border-width:1;"
                    : "-fx-background-color:white;-fx-padding:0 10;-fx-border-color:transparent transparent #f1f5f9 transparent;-fx-border-width:1;");

            // Rank badge
            Label rank = new Label(String.valueOf(row.getRank()));
            rank.setPrefWidth(40); rank.setMinHeight(48);
            rank.setAlignment(javafx.geometry.Pos.CENTER);
            rank.setStyle(rankBadgeStyle(row.getRank()));

            // Avatar + Tên
            String name = row.getFullName() != null ? row.getFullName() : "?";
            String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
            Label avatar = new Label(initial);
            avatar.setPrefSize(28, 28); avatar.setMinSize(28, 28);
            avatar.setAlignment(javafx.geometry.Pos.CENTER);
            avatar.setStyle("-fx-background-color:#4f46e5;-fx-text-fill:white;" +
                    "-fx-background-radius:50%;-fx-font-weight:bold;-fx-font-size:12px;");

            Label nameLabel = new Label(name);
            nameLabel.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1e293b;-fx-padding:0 0 0 8;");
            HBox nameBox = new HBox(avatar, nameLabel);
            nameBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            HBox.setHgrow(nameBox, javafx.scene.layout.Priority.ALWAYS);
            nameBox.setMaxWidth(Double.MAX_VALUE);

            // Số HĐ
            Label contracts = new Label(row.getContractCount() + " HĐ");
            contracts.setPrefWidth(65);
            contracts.setAlignment(javafx.geometry.Pos.CENTER);
            contracts.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#2563eb;");

            // Doanh thu
            Label rev = new Label(ReportBLL.formatMoneyFull(row.getRevenue()));
            rev.setPrefWidth(115);
            rev.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            rev.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#dc2626;");

            hbox.getChildren().addAll(rank, nameBox, contracts, rev);
            vboxTopStaff.getChildren().add(hbox);
        }
    }

    private String rankBadgeStyle(int rank) {
        String base = "-fx-font-weight:bold;-fx-font-size:13px;-fx-alignment:CENTER;";
        return base + switch (rank) {
            case 1 -> "-fx-text-fill:#b45309;";
            case 2 -> "-fx-text-fill:#475569;";
            case 3 -> "-fx-text-fill:#be185d;";
            default -> "-fx-text-fill:#94a3b8;";
        };
    }

    // ─────────────────────────────────────────────────────
    //  TỔNG KẾT THÁNG HIỆN TẠI
    // ─────────────────────────────────────────────────────
    private void refreshMonthSummary() {
        try {
            int displayMonth = selectedMonth > 0 ? selectedMonth : LocalDate.now().getMonthValue();
            int displayYear  = selectedYear;

            setLabel(lblCurrentMonthLabel, "T" + displayMonth + "/" + displayYear);

            double rev = reportBLL.getRevenueByMonth(displayYear, displayMonth);
            int contracts   = reportBLL.getContractsByMonth(displayYear, displayMonth);
            int customers   = reportBLL.getCustomersByMonth(displayYear, displayMonth);
            double prevRev  = reportBLL.getPrevMonthRevenue(displayYear, displayMonth);
            int prevContracts = reportBLL.getPrevMonthContracts(displayYear, displayMonth);
            double growthPct  = reportBLL.getMonthGrowthPercent(displayYear, displayMonth);

            setLabel(lblMonthRevenue,       ReportBLL.formatMoneySmart(rev));
            setLabel(lblMonthContracts,     String.valueOf(contracts));
            setLabel(lblMonthCustomers,     String.valueOf(customers));
            setLabel(lblPrevMonthRevenue,   ReportBLL.formatMoneySmart(prevRev));
            setLabel(lblPrevMonthContracts, String.valueOf(prevContracts));

            if (lblGrowthBadge != null) {
                String growthText = ReportBLL.formatPercent(growthPct);
                lblGrowthBadge.setText(growthText);
                lblGrowthBadge.setStyle(growthPct >= 0
                        ? "-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#16a34a;"
                        : "-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#dc2626;");
            }
        } catch (Exception ex) {
            System.err.println("[Report] MonthSummary: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────
    //  IN BÁO CÁO PDF
    // ─────────────────────────────────────────────────────
    @FXML
    void handleExportPdf(ActionEvent event) {
        try {
            Window window = null;
            if (lblYear != null && lblYear.getScene() != null)
                window = lblYear.getScene().getWindow();

            // Thu thập dữ liệu hiện tại
            int displayMonth = selectedMonth > 0 ? selectedMonth : 0;

            ReportPrinter.PrintData data = new ReportPrinter.PrintData();
            data.year        = selectedYear;
            data.month       = displayMonth;
            data.totalRev    = reportBLL.getTotalRevenue(selectedYear);
            data.growthPct   = reportBLL.getRevenueGrowthPercent(selectedYear);
            data.totalContracts = reportBLL.getTotalContracts(selectedYear);
            data.totalCustomers = reportBLL.getTotalCustomers(selectedYear);
            data.avgMonthly  = reportBLL.getAvgMonthlyRevenue(selectedYear);
            data.monthlyRevMap = reportBLL.getMonthlyRevenue(selectedYear);
            data.vehStatus   = reportBLL.getVehicleStatusCount();

            if (displayMonth > 0) {
                data.topVehicles = reportBLL.getTopVehiclesByMonth(selectedYear, displayMonth);
                data.topStaff    = reportBLL.getTopStaffByMonth(selectedYear, displayMonth);
                data.monthRev    = reportBLL.getRevenueByMonth(selectedYear, displayMonth);
                data.monthContracts = reportBLL.getContractsByMonth(selectedYear, displayMonth);
            } else {
                data.topVehicles = reportBLL.getTopVehiclesByYear(selectedYear);
                data.topStaff    = reportBLL.getTopStaffByYear(selectedYear);
                data.monthRev    = 0;
                data.monthContracts = 0;
            }

            String savedPath = ReportPrinter.print(data, window);
            if (savedPath != null) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Xuất báo cáo thành công");
                info.setHeaderText("✅ Đã lưu báo cáo PDF");
                info.setContentText("Đường dẫn:\n" + savedPath
                        + "\n\nBạn có muốn mở file không?");
                info.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                info.showAndWait().ifPresent(r -> {
                    if (r == ButtonType.YES) {
                        try {
                            java.awt.Desktop.getDesktop().open(new java.io.File(savedPath));
                        } catch (Exception ignored) {}
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Lỗi xuất PDF: " + ex.getMessage()).showAndWait();
        }
    }


    // ─────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────
    private String rankStyle(int rank) {
        return switch (rank) {
            case 1 -> "-fx-background-color:#fef3c7;-fx-text-fill:#b45309;";
            case 2 -> "-fx-background-color:#e2e8f0;-fx-text-fill:#475569;";
            case 3 -> "-fx-background-color:#fce7f3;-fx-text-fill:#be185d;";
            default -> "-fx-background-color:#f1f5f9;-fx-text-fill:#94a3b8;";
        };
    }

    private void setLabel(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }
}