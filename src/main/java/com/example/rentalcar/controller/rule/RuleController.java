package com.example.rentalcar.controller.rule;

import com.example.rentalcar.models.Rules;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.TilePane;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RuleController {

    @FXML private TilePane ruleContainer;

    @FXML
    public void initialize() {
        loadRuleCards();
    }

    public void loadRuleCards() {
        ruleContainer.getChildren().clear();

        // 2. Lấy dữ liệu (Mô phỏng lấy từ RuleBLL)
        // TODO: List<Rules> rulesList = ruleBLL.getAllRules();

    }

    @FXML
    void handleAddNewRule() {
        System.out.println("Mở Modal Thêm Quy luật mới!");
        // TODO: Xử lý mở popup thêm luật tại đây
    }

    // Hàm tạo dữ liệu giả chạy thử (Xóa đi khi có Database thật)
    private List<Rules> getMockData() {
        Rules rules = new Rules();
        List<Rules> li = new ArrayList<>();
        li.add(rules);
        return li;
    }
}