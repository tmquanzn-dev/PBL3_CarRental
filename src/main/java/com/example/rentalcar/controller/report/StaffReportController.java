package com.example.rentalcar.controller.report;

import com.example.rentalcar.bll.StaffReportBLL;
import com.example.rentalcar.models.StatusContracts;
import com.example.rentalcar.utils.AppSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class StaffReportController implements Initializable {
    @FXML private Label lblStaffName;
    @FXML private Label lblYearRevenue;
    @FXML private Label lblYearContracts;
    @FXML private Label lblAvgMonthly;
    @FXML private Label lblTotalPenalties;
    @FXML private Label lblRankBadge;

    @FXML private Label lblYear;
    @FXML private ComboBox<String> cbMonth;

    @FXML private BarChart<String, Number> barChartRevenue;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label lblChartSubtitle;
    @FXML private Label lblPeakMonth;

    @FXML private Label lblCurrentMonthLabel;
    @FXML private Label lblMonthRevenue;
    @FXML private Label lblMonthContracts;
    @FXML private Label lblGrowthBadge;
    @FXML private Label lblPrevMonthRevenue;
    @FXML private Label lblPrevMonthContracts;

    @FXML private VBox vboxRecentContracts;
    @FXML private Label lblNoContracts;

    private final StaffReportBLL staffReportBLL = new StaffReportBLL();

    private int selectedYear  = LocalDate.now().getYear();
    private int selectedMonth = LocalDate.now().getMonthValue();
    private int currentUserId;

    private static final String[] MONTH_LABELS = {
            "T1","T2","T3","T4","T5","T6",
            "T7","T8","T9","T10","T11","T12"
    };
    private static final String[] MONTH_NAMES = {
            "Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
            "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Lấy userId của người đang đăng nhập
        currentUserId = AppSession.getCurrentUser() != null
                ? AppSession.getCurrentUser().getId_user() : -1;

        setupMonthCombo();
        setupBarChart();
        loadAllData();
    }


    private void setupMonthCombo() {
        if (cbMonth == null) return;
        cbMonth.getItems().add("Cả năm");
        for (String mn : MONTH_NAMES) cbMonth.getItems().add(mn);
        cbMonth.getSelectionModel().select(selectedMonth);
        cbMonth.setOnAction(e -> {
            int idx = cbMonth.getSelectionModel().getSelectedIndex();
            selectedMonth = idx;
            refreshMonthSummary();
        });
    }

    private void setupBarChart() {
        if (barChartRevenue == null) return;
        barChartRevenue.setAnimated(false);
        barChartRevenue.setLegendVisible(false);
        if (xAxis != null) xAxis.setLabel("");
        if (yAxis != null) yAxis.setLabel("(tr.đ)");
    }


    @FXML void handlePrevYear(ActionEvent e) {
        selectedYear--;
        loadAllData();
    }
    @FXML void handleNextYear(ActionEvent e) {
        selectedYear++;
        loadAllData(); }

    @FXML void handleRefresh(ActionEvent e) {
        selectedYear  = LocalDate.now().getYear();
        selectedMonth = LocalDate.now().getMonthValue();
        if (cbMonth != null) cbMonth.getSelectionModel().select(selectedMonth);
        loadAllData();
    }

    private void loadAllData() {
        if (currentUserId <= 0) return;
        setLabel(lblYear, String.valueOf(selectedYear));

        // Tên nhân viên
        if (lblStaffName != null && AppSession.getCurrentUser() != null) {
            lblStaffName.setText("Báo cáo của: " + AppSession.getCurrentUser().getFull_name());
        }

        loadKpiCards();
        loadBarChart();
        loadRankBadge();
        loadRecentContracts();
        refreshMonthSummary();
    }

    private void loadKpiCards() {
        try {
            double totalRev = staffReportBLL.getTotalRevenue(currentUserId, selectedYear);
            setLabel(lblYearRevenue, StaffReportBLL.formatMoney(totalRev));

            int totalContracts = staffReportBLL.getTotalContracts(currentUserId, selectedYear);
            setLabel(lblYearContracts, String.valueOf(totalContracts));

            setLabel(lblAvgMonthly, StaffReportBLL.formatMoney(
                    staffReportBLL.getAvgMonthlyRevenue(currentUserId, selectedYear)));

            double penalties = staffReportBLL.getTotalPenalties(currentUserId, selectedYear);
            setLabel(lblTotalPenalties, StaffReportBLL.formatMoney(penalties));

        } catch (Exception ex) {
            System.err.println("[StaffReport] KPI: " + ex.getMessage());
        }
    }


    private void loadRankBadge() {
        try {
            int rank = staffReportBLL.getRankThisMonth(currentUserId);
            if (lblRankBadge == null) return;
            if (rank <= 0) {
                lblRankBadge.setText("Chưa có dữ liệu");
            } else if (rank == 1) {
                lblRankBadge.setText("🥇 Hạng 1 tháng này!");
                lblRankBadge.setStyle("-fx-text-fill: #b45309; -fx-font-weight: bold;");
            } else if (rank == 2) {
                lblRankBadge.setText("🥈 Hạng 2 tháng này");
                lblRankBadge.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
            } else if (rank == 3) {
                lblRankBadge.setText("🥉 Hạng 3 tháng này");
                lblRankBadge.setStyle("-fx-text-fill: #be185d; -fx-font-weight: bold;");
            } else {
                lblRankBadge.setText("Hạng " + rank + " tháng này");
                lblRankBadge.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
            }
        } catch (Exception ex) {
            System.err.println("[StaffReport] Rank: " + ex.getMessage());
        }
    }


    private void loadBarChart() {
        if (barChartRevenue == null) return;
        try {
            barChartRevenue.getData().clear();
            Map<Integer, Double> revData = staffReportBLL.getMonthlyRevenue(currentUserId, selectedYear);
            Map<Integer, Integer> cntData = staffReportBLL.getMonthlyContracts(currentUserId, selectedYear);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu");

            double peak = 0, total = 0;
            int peakMonth = 1;

            for (int m = 1; m <= 12; m++) {
                double rev = revData.getOrDefault(m, 0.0);
                series.getData().add(new XYChart.Data<>(MONTH_LABELS[m-1], rev / 1_000_000.0));
                total += rev;
                if (rev > peak) { peak = rev; peakMonth = m; }
            }

            barChartRevenue.getData().add(series);
            setLabel(lblChartSubtitle, "Tổng: " + StaffReportBLL.formatMoney(total) + "  •  Đơn vị: triệu đồng");

            if (lblPeakMonth != null) {
                if (peak > 0) {
                    lblPeakMonth.setText("🏆 Đỉnh: " + MONTH_LABELS[peakMonth-1]
                            + " (" + StaffReportBLL.formatMoney(peak) + ")");
                    lblPeakMonth.setVisible(true);
                } else {
                    lblPeakMonth.setVisible(false);
                }
            }

            // Tô màu bar
            final int pkMonth = peakMonth;
            Platform.runLater(() -> {
                for (int i = 0; i < series.getData().size(); i++) {
                    XYChart.Data<String, Number> d = series.getData().get(i);
                    if (d.getNode() != null) {
                        String color = (i + 1 == pkMonth) ? "#ef4444" : "#146dff";
                        d.getNode().setStyle("-fx-bar-fill: " + color + "; -fx-background-radius: 4 4 0 0;");
                    }
                }
            });

        } catch (Exception ex) {
            System.err.println("[StaffReport] BarChart: " + ex.getMessage());
        }
    }

    private void refreshMonthSummary() {
        try {
            int displayMonth = selectedMonth > 0 ? selectedMonth : LocalDate.now().getMonthValue();
            int displayYear  = selectedYear;

            setLabel(lblCurrentMonthLabel, "T" + displayMonth + "/" + displayYear);

            double rev      = staffReportBLL.getRevenueByMonth(currentUserId, displayYear, displayMonth);
            int contracts   = staffReportBLL.getContractsByMonth(currentUserId, displayYear, displayMonth);

            // Tháng trước
            int prevYear = displayMonth == 1 ? displayYear - 1 : displayYear;
            int prevMonth = displayMonth == 1 ? 12 : displayMonth - 1;
            double prevRev = staffReportBLL.getRevenueByMonth(currentUserId, prevYear, prevMonth);
            int prevContracts = staffReportBLL.getContractsByMonth(currentUserId, prevYear, prevMonth);
            double growthPct = staffReportBLL.getGrowthPercent(currentUserId, displayYear, displayMonth);

            setLabel(lblMonthRevenue, StaffReportBLL.formatMoney(rev));
            setLabel(lblMonthContracts, String.valueOf(contracts));
            setLabel(lblPrevMonthRevenue, StaffReportBLL.formatMoney(prevRev));
            setLabel(lblPrevMonthContracts, String.valueOf(prevContracts));

            if (lblGrowthBadge != null) {
                lblGrowthBadge.setText(StaffReportBLL.formatPercent(growthPct));
                lblGrowthBadge.setStyle(growthPct >= 0
                        ? "-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#16a34a;"
                        : "-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#dc2626;");
            }
        } catch (Exception ex) {
            System.err.println("[StaffReport] MonthSummary: " + ex.getMessage());
        }
    }

    private void loadRecentContracts() {
        if (vboxRecentContracts == null) return;
        vboxRecentContracts.getChildren().clear();

        try {
            List<Map<String, Object>> list = staffReportBLL.getRecentContracts(currentUserId);

            if (list == null || list.isEmpty()) {
                if (lblNoContracts != null) {
                    lblNoContracts.setVisible(true);
                    lblNoContracts.setManaged(true);
                }
                return;
            }

            if (lblNoContracts != null) {
                lblNoContracts.setVisible(false);
                lblNoContracts.setManaged(false);
            }

            for (Map<String, Object> row : list) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/report/RecentContractRow.fxml"));
                    javafx.scene.Parent rowUi = loader.load();

                    RecentContractRowController ctrl = loader.getController();
                    ctrl.setRowData(row);
                    vboxRecentContracts.getChildren().add(rowUi);
                } catch (Exception e) {
                    System.err.println("Lỗi nạp FXML dòng đơn hàng gần đây: " + e.getMessage());
                }
            }

        } catch (Exception ex) {
            System.err.println("[StaffReport] RecentContracts: " + ex.getMessage());
        }
    }

    //  HELPERS
    private String mapStatus(String s) {
        if (s == null) return "--";
        return switch (s.replace(" ", "_")) {
            case "DANG_THUE"  -> "Đang thuê";
            case "HOAN_THANH" -> "Hoàn thành";
            case "QUA_HAN"    -> "Quá hạn";
            case "DA_HUY"     -> "Đã hủy";
            default           -> s;
        };
    }

    private String mapStatusStyle(String s) {
        if (s == null) return "-fx-background-color:#f1f5f9;-fx-text-fill:#64748b;";
        return switch (s.replace(" ", "_")) {
            case "DANG_THUE"  -> "-fx-background-color:#dbeafe;-fx-text-fill:#1d4ed8;";
            case "HOAN_THANH" -> "-fx-background-color:#dcfce7;-fx-text-fill:#15803d;";
            case "QUA_HAN"    -> "-fx-background-color:#fef3c7;-fx-text-fill:#b45309;";
            case "DA_HUY"     -> "-fx-background-color:#f1f5f9;-fx-text-fill:#64748b;";
            default           -> "-fx-background-color:#f1f5f9;-fx-text-fill:#64748b;";
        };
    }

    private void setLabel(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }
}