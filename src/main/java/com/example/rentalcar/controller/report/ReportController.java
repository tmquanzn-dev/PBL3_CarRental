package com.example.rentalcar.controller.report;

import com.example.rentalcar.bll.ReportBLL;
import com.example.rentalcar.bll.VehicleBLL;
import com.example.rentalcar.models.StaffReportRow;
import com.example.rentalcar.models.VehicleReportRow;
import com.example.rentalcar.utils.ReportPrinter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
    @FXML private TableView<VehicleReportRow>            tableTopVehicles;
    @FXML private TableColumn<VehicleReportRow, Integer> colVRank;
    @FXML private TableColumn<VehicleReportRow, String>  colVName;
    @FXML private TableColumn<VehicleReportRow, String>  colVPlate;
    @FXML private TableColumn<VehicleReportRow, String>  colVCount;
    @FXML private TableColumn<VehicleReportRow, Double>  colVRevenue;
    @FXML private Label lblTopVehiclesPeriod;

    // ── Top 5 nhân viên ───────────────────────────────────
    @FXML private TableView<StaffReportRow>              tableTopStaff;
    @FXML private TableColumn<StaffReportRow, Integer>   colSRank;
    @FXML private TableColumn<StaffReportRow, String>    colSName;
    @FXML private TableColumn<StaffReportRow, String>    colSContracts;
    @FXML private TableColumn<StaffReportRow, Double>    colSRevenue;
    @FXML private Label lblTopStaffPeriod;

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
        setupTableColumns();
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
    //  TOP 5 (theo tháng hoặc năm)
    // ─────────────────────────────────────────────────────
    private void refreshTopTables() {
        try {
            boolean isMonthMode = selectedMonth > 0;
            String periodLabel = isMonthMode
                    ? MONTH_NAMES[selectedMonth - 1] + "/" + selectedYear
                    : "Năm " + selectedYear;

            // Cập nhật badge
            setLabel(lblTopVehiclesPeriod, periodLabel);
            setLabel(lblTopStaffPeriod,    periodLabel);

            // Load xe
            List<VehicleReportRow> vList = isMonthMode
                    ? reportBLL.getTopVehiclesByMonth(selectedYear, selectedMonth)
                    : reportBLL.getTopVehiclesByYear(selectedYear);
            tableTopVehicles.setItems(FXCollections.observableArrayList(vList));
            tableTopVehicles.setFixedCellSize(52.0);
            tableTopVehicles.refresh();

            // Load nhân viên
            List<StaffReportRow> sList = isMonthMode
                    ? reportBLL.getTopStaffByMonth(selectedYear, selectedMonth)
                    : reportBLL.getTopStaffByYear(selectedYear);
            tableTopStaff.setItems(FXCollections.observableArrayList(sList));
            tableTopStaff.setFixedCellSize(52.0);
            tableTopStaff.refresh();

        } catch (Exception ex) {
            System.err.println("[Report] TopTables: " + ex.getMessage());
        }
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
    //  SETUP TABLE COLUMNS
    // ─────────────────────────────────────────────────────
    private void setupTableColumns() {

        // ── TOP XE ──────────────────────────────────────
        colVRank.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null); setText(null);
                if (!empty && getTableRow() != null && getTableRow().getItem() != null) {
                    VehicleReportRow row = (VehicleReportRow) getTableRow().getItem();
                    Label b = new Label(String.valueOf(row.getRank()));
                    b.setPrefSize(28, 28);
                    b.setAlignment(Pos.CENTER);
                    b.setStyle("-fx-background-radius:50%;-fx-font-weight:bold;" + rankStyle(row.getRank()));
                    setGraphic(b);
                }
            }
        });

        colVName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getVehicleName()));
        colVName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) { setText(null); setGraphic(null); return; }
                setText(name);
                setStyle("-fx-font-weight:bold;-fx-text-fill:#1e293b;-fx-font-size:13px;");
            }
        });

        colVPlate.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getPlateNumber()));
        colVPlate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String plate, boolean empty) {
                super.updateItem(plate, empty);
                if (empty || plate == null) { setGraphic(null); setText(null); return; }
                Label lbl = new Label(plate);
                lbl.setStyle("-fx-background-color:#f1f5f9;-fx-text-fill:#475569;" +
                        "-fx-padding:2 8;-fx-background-radius:6;-fx-font-size:12px;-fx-font-weight:bold;");
                setGraphic(lbl); setText(null);
            }
        });

        colVCount.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getRentalCount() + " lượt"));
        colVCount.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String count, boolean empty) {
                super.updateItem(count, empty);
                if (empty || count == null) { setText(null); return; }
                setText(count);
                setStyle("-fx-text-fill:#2563eb;-fx-font-weight:bold;-fx-font-size:13px;");
            }
        });

        colVRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        colVRevenue.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(ReportBLL.formatMoneyFull(item));
                setStyle("-fx-font-weight:bold;-fx-text-fill:#dc2626;-fx-font-size:12px;");
            }
        });

        // ── TOP NHÂN VIÊN ────────────────────────────────
        colSRank.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null); setText(null);
                if (!empty && getTableRow() != null && getTableRow().getItem() != null) {
                    StaffReportRow row = (StaffReportRow) getTableRow().getItem();
                    Label b = new Label(String.valueOf(row.getRank()));
                    b.setPrefSize(28, 28);
                    b.setAlignment(Pos.CENTER);
                    b.setStyle("-fx-background-radius:50%;-fx-font-weight:bold;" + rankStyle(row.getRank()));
                    setGraphic(b);
                }
            }
        });

        colSName.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFullName()));
        colSName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Circle av = new Circle(13, Color.web("#4f46e5"));
                Text t = new Text(item.isEmpty() ? "?" : item.substring(0, 1).toUpperCase());
                t.setFill(Color.WHITE);
                t.setStyle("-fx-font-size:11px;-fx-font-weight:bold;");
                StackPane sp = new StackPane(av, t);
                Label l = new Label(item);
                l.setStyle("-fx-font-weight:bold;-fx-text-fill:#1e293b;-fx-font-size:13px;");
                HBox box = new HBox(8, sp, l);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box); setText(null);
            }
        });

        colSContracts.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getContractCount() + " HĐ"));
        colSContracts.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String count, boolean empty) {
                super.updateItem(count, empty);
                if (empty || count == null) { setText(null); return; }
                setText(count);
                setStyle("-fx-text-fill:#2563eb;-fx-font-weight:bold;-fx-font-size:13px;");
            }
        });

        colSRevenue.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        colSRevenue.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(ReportBLL.formatMoneyFull(item));
                setStyle("-fx-font-weight:bold;-fx-text-fill:#dc2626;-fx-font-size:12px;");
            }
        });
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