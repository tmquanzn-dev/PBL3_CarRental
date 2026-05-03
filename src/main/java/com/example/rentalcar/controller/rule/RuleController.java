package com.example.rentalcar.controller.rule;

import com.example.rentalcar.bll.RuleBLL;
import com.example.rentalcar.models.Rules;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RuleController – Màn hình quản lý luật tính giá (Admin only).
 * Load và hiển thị danh sách luật dưới dạng card.
 */
public class RuleController {

    // ── FXML ─────────────────────────────────────────────────────────
    @FXML private FlowPane ruleContainer;
    @FXML private TextField txtSearch;

    // Cards thống kê
    @FXML private Label lblTotalRules;
    @FXML private Label lblActiveRules;
    @FXML private Label lblApplicableToday;

    private final RuleBLL ruleBLL = new RuleBLL();

    // ================================================================
    // INITIALIZE
    // ================================================================
    @FXML
    public void initialize() {
        loadRuleCards();
    }

    // ================================================================
    // LOAD CARDS
    // ================================================================
    public void loadRuleCards() {
        loadRuleCards(null); // null = không lọc
    }

    private void loadRuleCards(String keyword) {
        if (ruleContainer == null) return;
        ruleContainer.getChildren().clear();

        List<Rules> list = ruleBLL.getAllRules();

        // Lọc theo keyword nếu có
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            list = list.stream()
                    .filter(r -> r.getRule_name() != null
                            && r.getRule_name().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }

        // Cập nhật cards thống kê
        updateStats();

        if (list.isEmpty()) {
            Label empty = new Label("😔  Chưa có luật tính giá nào được tạo.");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-padding: 30;");
            ruleContainer.getChildren().add(empty);
            return;
        }

        for (Rules rule : list) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/views/rule/RuleCard.fxml"));
                Parent card = loader.load();

                RuleCardController cardCtrl = loader.getController();
                cardCtrl.setData(rule, this::loadRuleCards); // callback reload

                ruleContainer.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("Lỗi render RuleCard: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ================================================================
    // THỐNG KÊ
    // ================================================================
    private void updateStats() {
        if (lblTotalRules != null)
            lblTotalRules.setText(String.valueOf(ruleBLL.getTotalRules()));
        if (lblActiveRules != null)
            lblActiveRules.setText(String.valueOf(ruleBLL.getActiveCount()));
        if (lblApplicableToday != null)
            lblApplicableToday.setText(String.valueOf(ruleBLL.getApplicableTodayCount()));
    }

    // ================================================================
    // TÌM KIẾM
    // ================================================================
    @FXML
    void handleSearch(ActionEvent event) {
        String keyword = txtSearch != null ? txtSearch.getText().trim() : "";
        loadRuleCards(keyword);
    }

    @FXML
    void handleReload(ActionEvent event) {
        if (txtSearch != null) txtSearch.clear();
        loadRuleCards();
    }

    // ================================================================
    // MỞ MODAL THÊM LUẬT MỚI
    // ================================================================
    @FXML
    void handleAddNewRule() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/rule/RuleFormModal.fxml"));
            Parent root = loader.load();

            RuleFormController ctrl = loader.getController();
            ctrl.setMode(null); // ADD mode
            ctrl.setOnSaved(this::loadRuleCards); // reload sau khi lưu

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Thêm luật tính giá mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();

        } catch (Exception e) {
            System.err.println("Lỗi mở form thêm luật: " + e.getMessage());
            e.printStackTrace();
        }
    }
}