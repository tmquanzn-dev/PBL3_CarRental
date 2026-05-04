package com.example.rentalcar.controller.rule;

import com.example.rentalcar.bll.RuleBLL;
import com.example.rentalcar.models.Rules;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.TilePane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class RuleController {

    @FXML private TilePane ruleContainer;
    private final RuleBLL ruleBLL = new RuleBLL(); // Gọi BLL

    @FXML
    public void initialize() {
        loadRuleCards();
    }

    public void loadRuleCards() {
        ruleContainer.getChildren().clear();

        try {
            // Lấy danh sách luật thật từ Database thông qua BLL
            List<Rules> rulesList = ruleBLL.getAllRules();

            for (Rules rule : rulesList) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/rule/RuleCard.fxml"));
                Parent card = loader.load();

                RuleCardController cardCtrl = loader.getController();
                // Truyền dữ liệu vào Card và ép nó tự refresh lại danh sách nếu có thao tác Xóa/Tắt bật
                cardCtrl.setData(rule, this::loadRuleCards);

                ruleContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi load danh sách Luật: " + e.getMessage());
        }
    }

    @FXML
    void handleAddNewRule() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/rule/RuleFormModal.fxml"));
            Parent root = loader.load();

            RuleFormController ctrl = loader.getController();
            ctrl.setMode(null); // NULL là chế độ Thêm mới
            ctrl.setOnSaved(this::loadRuleCards); // Lưu xong thì tải lại danh sách

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