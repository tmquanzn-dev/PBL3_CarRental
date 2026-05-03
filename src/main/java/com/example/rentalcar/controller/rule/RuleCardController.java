package com.example.rentalcar.controller.rule;

import com.example.rentalcar.bll.RuleBLL;
import com.example.rentalcar.models.RuleType;
import com.example.rentalcar.models.Rules;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class RuleCardController {

    @FXML private Label       lblType;
    @FXML private Label       lblMulti;
    @FXML private Label       lblName;
    @FXML private Label       lblDate;
    @FXML private Label       lblStatusBadge;
    @FXML private AnchorPane  accentBar;  // thanh màu đầu card

    private Rules    currentRule;
    private Runnable onRefresh;

    private final RuleBLL ruleBLL = new RuleBLL();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ================================================================
    // NHẬN DỮ LIỆU
    // ================================================================
    public void setData(Rules rule, Runnable onRefresh) {
        this.currentRule = rule;
        this.onRefresh   = onRefresh;
        if (rule == null) return;

        // ── Tên luật ─────────────────────────────────────────────
        if (lblName != null) lblName.setText(rule.getRule_name());

        // ── Hệ số nhân ───────────────────────────────────────────
        if (lblMulti != null)
            lblMulti.setText(RuleBLL.formatMultiplier(rule.getMulti()));

        // ── Màu accent bar + badge loại ──────────────────────────
        RuleType rt = rule.getRule_type();
        String accentColor, badgeBg, badgeFg, typeText;
        if (rt == RuleType.CUOI_TUAN) {
            accentColor = "#146dff"; badgeBg = "#dbeafe"; badgeFg = "#1d4ed8";
            typeText = "CUỐI TUẦN";
        } else if (rt == RuleType.NGAY_LE) {
            accentColor = "#be185d"; badgeBg = "#fce7f3"; badgeFg = "#be185d";
            typeText = "NGÀY LỄ";
        } else {
            accentColor = "#64748b"; badgeBg = "#f1f5f9"; badgeFg = "#475569";
            typeText = "KHÁC";
        }

        if (accentBar != null)
            accentBar.setStyle("-fx-background-color: " + accentColor
                    + "; -fx-background-radius: 12 12 0 0;");

        if (lblType != null)
            lblType.setStyle("-fx-background-color: " + badgeBg + "; -fx-text-fill: " + badgeFg
                    + "; -fx-padding: 3 10; -fx-background-radius: 20;"
                    + " -fx-font-weight: bold; -fx-font-size: 10px;");
        if (lblType != null) lblType.setText(typeText);

        // Màu hệ số nhân đồng bộ với accent
        if (lblMulti != null)
            lblMulti.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");

        // ── Ngày áp dụng ─────────────────────────────────────────
        if (lblDate != null) {
            String start = rule.getStart_date() != null
                    ? rule.getStart_date().toLocalDate().format(DATE_FMT) : "--";
            String end = rule.getEnd_date() != null
                    ? rule.getEnd_date().toLocalDate().format(DATE_FMT) : "--";
            lblDate.setText("📅  " + start + "  →  " + end);
        }

        // ── Trạng thái ───────────────────────────────────────────
        if (lblStatusBadge != null) {
            if (rule.isIs_active()) {
                lblStatusBadge.setText("● Bật");
                lblStatusBadge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;"
                        + " -fx-padding: 3 10; -fx-background-radius: 20;"
                        + " -fx-font-weight: bold; -fx-font-size: 10px;");
            } else {
                lblStatusBadge.setText("○ Tắt");
                lblStatusBadge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;"
                        + " -fx-padding: 3 10; -fx-background-radius: 20;"
                        + " -fx-font-weight: bold; -fx-font-size: 10px;");
            }
        }
    }

    // ================================================================
    // SỬA
    // ================================================================
    @FXML
    void handleEdit() {
        if (currentRule == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/rule/RuleFormModal.fxml"));
            Parent root = loader.load();
            RuleFormController ctrl = loader.getController();
            ctrl.setMode(currentRule);
            ctrl.setOnSaved(() -> { if (onRefresh != null) onRefresh.run(); });
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Sửa luật: " + currentRule.getRule_name());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (Exception e) {
            showError("Lỗi mở form sửa luật: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ================================================================
    // XÓA
    // ================================================================
    @FXML
    void handleDelete() {
        if (currentRule == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Xóa luật \"" + currentRule.getRule_name() + "\"?\nThao tác không thể hoàn tác!");
        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                if (ruleBLL.deleteRule(currentRule.getId_rule()) && onRefresh != null)
                    onRefresh.run();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    // ================================================================
    // BẬT / TẮT
    // ================================================================
    @FXML
    void handleToggle() {
        if (currentRule == null) return;
        try {
            if (ruleBLL.toggleRule(currentRule.getId_rule()) && onRefresh != null)
                onRefresh.run();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Lỗi"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}