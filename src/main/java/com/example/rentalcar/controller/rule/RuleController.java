package com.example.rentalcar.controller.rule;

import com.example.rentalcar.bll.RuleBLL;
import com.example.rentalcar.models.Rules;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class RuleController implements Initializable {
    @FXML private Label lblTotalRules;
    @FXML private Label lblActiveRules;
    @FXML private Label lblApplicableToday;
    @FXML private TextField txtSearch;
    @FXML private FlowPane ruleContainer;

    private final RuleBLL ruleBLL = new RuleBLL();
    private List<Rules> masterList; // Lưu danh sách gốc để tìm kiếm nhanh

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadData();
    }

    public void loadData() {
        try {
            masterList = ruleBLL.getAllRules();

            if (lblTotalRules != null)
                lblTotalRules.setText(String.valueOf(masterList.size()));

            if (lblActiveRules != null)
                lblActiveRules.setText(String.valueOf(ruleBLL.getActiveCount()));

            if (lblApplicableToday != null)
                lblApplicableToday.setText(String.valueOf(ruleBLL.getApplicableTodayCount()));

            renderRuleCards(masterList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderRuleCards(List<Rules> list) {
        ruleContainer.getChildren().clear();
        if (list == null || list.isEmpty()) return;

        for (Rules rule : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/rule/RuleCard.fxml"));
                Parent card = loader.load();
                RuleCardController cardCtrl = loader.getController();
                cardCtrl.setData(rule, this::loadData);

                ruleContainer.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("Lỗi load RuleCard: " + e.getMessage());
            }
        }
    }


    @FXML
    void handleSearch() {
        if (masterList == null)
            return;

        String keyword = txtSearch.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            renderRuleCards(masterList);
            return;
        }

        // Lọc danh sách theo tên luật hoặc loại luật (Ngày lễ, Cuối tuần...)
        List<Rules> filtered = masterList.stream()
                .filter(r -> (r.getRule_name() != null && r.getRule_name().toLowerCase().contains(keyword))
                        || (r.getRuleTypeDisplay() != null && r.getRuleTypeDisplay().toLowerCase().contains(keyword)))
                .collect(Collectors.toList());

        renderRuleCards(filtered);
    }

    @FXML
    void handleReload() {
        if (txtSearch != null) txtSearch.clear();
        loadData();
    }

    @FXML
    void handleAddNewRule() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/rule/RuleFormModal.fxml"));
            Parent root = loader.load();

            RuleFormController ctrl = loader.getController();
            ctrl.setMode(null);
            ctrl.setOnSaved(this::loadData); // Callback để load lại bảng sau khi lưu thành công

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Thêm luật tính giá mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}