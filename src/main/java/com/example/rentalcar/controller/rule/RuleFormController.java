package com.example.rentalcar.controller.rule;

import com.example.rentalcar.bll.RuleBLL;
import com.example.rentalcar.models.RuleType;
import com.example.rentalcar.models.Rules;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * RuleFormController – Controller cho modal Thêm / Sửa luật tính giá.
 * Gọi setMode(null) để thêm mới, setMode(rule) để sửa.
 */
public class RuleFormController implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────
    @FXML private Label         lblTitle, lblSubtitle;
    @FXML private TextField     txtRuleName;
    @FXML private ComboBox<String> cbRuleType;
    @FXML private TextField     txtMulti;
    @FXML private Label         lblMultiPreview;
    @FXML private DatePicker    dpStart, dpEnd;
    @FXML private CheckBox      chkActive;
    @FXML private Label         lblMsg;
    @FXML private Button        btnSave;

    // ── STATE ─────────────────────────────────────────────────────────
    private final RuleBLL ruleBLL = new RuleBLL();
    private Rules editingRule = null;   // null = ADD mode
    private Runnable onSaved;           // callback reload danh sách

    // ================================================================
    // INITIALIZE
    // ================================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbRuleType.setItems(FXCollections.observableArrayList(
                "Cuối tuần", "Ngày lễ", "Khác"));
        cbRuleType.setValue("Cuối tuần");

        dpStart.setValue(LocalDate.now());
        dpEnd.setValue(LocalDate.now().plusMonths(1));

        chkActive.setSelected(true);

        // Live preview hệ số nhân
        txtMulti.textProperty().addListener((obs, old, val) -> updatePreview(val));
    }

    // ================================================================
    // SET MODE: ADD hoặc EDIT
    // ================================================================
    public void setMode(Rules rule) {
        this.editingRule = rule;

        if (rule == null) {
            // ADD
            setHeader("Thêm luật tính giá mới", "Cấu hình hệ số giá cho ngày lễ / cuối tuần");
            btnSave.setText("💾  Lưu luật");
        } else {
            // EDIT
            setHeader("Chỉnh sửa luật", "Cập nhật thông tin: " + rule.getRule_name());
            btnSave.setText("💾  Lưu thay đổi");

            txtRuleName.setText(rule.getRule_name());
            cbRuleType.setValue(rule.getRuleTypeDisplay());
            txtMulti.setText(String.valueOf(rule.getMulti()));
            if (rule.getStart_date() != null)
                dpStart.setValue(rule.getStart_date().toLocalDate());
            if (rule.getEnd_date() != null)
                dpEnd.setValue(rule.getEnd_date().toLocalDate());
            chkActive.setSelected(rule.isIs_active());
            updatePreview(String.valueOf(rule.getMulti()));
        }
    }

    public void setOnSaved(Runnable callback) { this.onSaved = callback; }

    // ================================================================
    // LƯU
    // ================================================================
    @FXML
    void handleSave() {
        String name = txtRuleName.getText().trim();
        if (name.isBlank()) {
            showMsg("❌  Vui lòng nhập tên luật!", false);
            txtRuleName.requestFocus();
            return;
        }

        String typeDisplay = cbRuleType.getValue();
        if (typeDisplay == null) {
            showMsg("❌  Vui lòng chọn loại luật!", false);
            return;
        }

        double multi;
        try {
            multi = Double.parseDouble(txtMulti.getText().trim().replace(",", "."));
            if (multi <= 0 || multi > 10) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showMsg("❌  Hệ số nhân phải là số > 0 và ≤ 10 (VD: 1.5)", false);
            txtMulti.requestFocus();
            return;
        }

        LocalDate startLocal = dpStart.getValue();
        LocalDate endLocal   = dpEnd.getValue();
        if (startLocal == null || endLocal == null) {
            showMsg("❌  Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc!", false);
            return;
        }
        if (endLocal.isBefore(startLocal)) {
            showMsg("❌  Ngày kết thúc phải sau ngày bắt đầu!", false);
            return;
        }

        RuleType ruleType = displayToRuleType(typeDisplay);
        Date start = Date.valueOf(startLocal);
        Date end   = Date.valueOf(endLocal);
        boolean active = chkActive.isSelected();

        try {
            if (editingRule == null) {
                // ADD
                Rules newRule = new Rules(0, name, ruleType, multi, start, end, active);
                boolean ok = ruleBLL.addRule(newRule);
                if (ok) { showMsg("✅  Thêm luật thành công!", true); autoClose(); }
                else    { showMsg("❌  Lưu thất bại, vui lòng thử lại!", false); }
            } else {
                // EDIT
                editingRule.setRule_name(name);
                editingRule.setRule_type(ruleType);
                editingRule.setMulti(multi);
                editingRule.setStart_date(start);
                editingRule.setEnd_date(end);
                editingRule.setIs_active(active);
                boolean ok = ruleBLL.updateRule(editingRule);
                if (ok) { showMsg("✅  Cập nhật thành công!", true); autoClose(); }
                else    { showMsg("❌  Cập nhật thất bại!", false); }
            }
        } catch (IllegalStateException | IllegalArgumentException ex) {
            // Gom cả 2 lỗi nghiệp vụ và bảo mật để hiện thông báo đỏ
            showMsg("❌ " + ex.getMessage(), false);
        } catch (Exception ex) {
            showMsg("❌ Lỗi hệ thống: " + ex.getMessage(), false);
        }
    }

    // ================================================================
    // CLOSE
    // ================================================================
    @FXML
    void handleClose() { getStage().close(); }

    // ================================================================
    // HELPERS
    // ================================================================

    private void setHeader(String title, String subtitle) {
        if (lblTitle    != null) lblTitle.setText(title);
        if (lblSubtitle != null) lblSubtitle.setText(subtitle);
    }

    private void updatePreview(String val) {
        if (lblMultiPreview == null) return;
        try {
            double v = Double.parseDouble(val.replace(",", "."));
            if (v <= 0) throw new NumberFormatException();
            lblMultiPreview.setText(String.format("→ Giá = giá gốc × %.2f  (tăng %.0f%%)", v, (v - 1) * 100));
            lblMultiPreview.setStyle("-fx-text-fill: #146dff; -fx-font-weight: bold; -fx-font-size: 13px;");
        } catch (NumberFormatException e) {
            lblMultiPreview.setText(val.isBlank() ? "" : "⚠  Nhập số hợp lệ (VD: 1.5)");
            lblMultiPreview.setStyle("-fx-text-fill: #e11d48;");
        }
    }

    private void showMsg(String text, boolean success) {
        if (lblMsg == null) return;
        lblMsg.setText(text);
        lblMsg.setStyle(success
                ? "-fx-text-fill: #16a34a; -fx-font-size: 13px; -fx-font-weight: bold;"
                : "-fx-text-fill: #e11d48; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblMsg.setVisible(true);
        new Timeline(new KeyFrame(Duration.seconds(3.5),
                e -> { if (lblMsg != null) lblMsg.setVisible(false); })).play();
    }

    private void autoClose() {
        if (onSaved != null) onSaved.run();
        new Timeline(new KeyFrame(Duration.seconds(1.2), e -> getStage().close())).play();
    }

    private Stage getStage() {
        return (Stage) btnSave.getScene().getWindow();
    }

    private RuleType displayToRuleType(String display) {
        return switch (display) {
            case "Cuối tuần" -> RuleType.CUOI_TUAN;
            case "Ngày lễ"   -> RuleType.NGAY_LE;
            default          -> RuleType.KHAC;
        };
    }
}