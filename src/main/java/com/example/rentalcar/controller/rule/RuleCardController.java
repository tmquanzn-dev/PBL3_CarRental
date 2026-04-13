package com.example.rentalcar.controller.rule;

import com.example.rentalcar.models.Rules;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;

public class RuleCardController {

    @FXML private Label lblType;
    @FXML private Label lblMulti;
    @FXML private Label lblName;
    @FXML private Label lblDate;

    private Rules currentRule;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Hàm nhận dữ liệu từ Controller mẹ truyền sang
    public void setData(Rules rule) {

    }

    @FXML
    void handleEdit() {
        if (currentRule != null) {
            System.out.println("Đang mở Form Sửa luật: " + currentRule.getRule_name());
            // TODO: Gọi Modal Edit Rule truyền currentRule sang
        }
    }

    @FXML
    void handleDelete() {
        if (currentRule != null) {
            System.out.println("Đang yêu cầu Xóa luật: " + currentRule.getRule_name());
            // TODO: Gọi hàm xóa của RuleBLL và thông báo Alert
        }
    }
}