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

/**
 * Controller quản lý danh sách Quy luật tính giá.
 * Tương thích 100% với RuleManagement.fxml
 */
public class RuleController implements Initializable {

    // ─── Các ID khai báo trong FXML ───
    @FXML private Label lblTotalRules;       // Card tổng số luật
    @FXML private Label lblActiveRules;      // Card luật đang bật
    @FXML private Label lblApplicableToday; // Card luật áp dụng hôm nay
    @FXML private TextField txtSearch;       // Ô tìm kiếm
    @FXML private FlowPane ruleContainer;    // Nơi chứa các Card quy luật

    private final RuleBLL ruleBLL = new RuleBLL();
    private List<Rules> masterList; // Lưu danh sách gốc để tìm kiếm nhanh

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vừa mở màn hình là nạp dữ liệu và thống kê ngay
        loadData();
    }

    // ================================================================
    // NẠP DỮ LIỆU & CẬP NHẬT GIAO DIỆN
    // ================================================================
    public void loadData() {
        try {
            // 1. Lấy dữ liệu mới nhất từ BLL
            masterList = ruleBLL.getAllRules();

            // 2. Cập nhật các con số thống kê trên các Card Header
            // Đảm bảo RuleBLL của em đã có các hàm count này
            if (lblTotalRules != null)
                lblTotalRules.setText(String.valueOf(masterList.size()));

            if (lblActiveRules != null)
                lblActiveRules.setText(String.valueOf(ruleBLL.getActiveCount()));

            if (lblApplicableToday != null)
                lblApplicableToday.setText(String.valueOf(ruleBLL.getApplicableTodayCount()));

            // 3. Hiển thị danh sách các thẻ luật
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
                // Nạp file giao diện cho từng thẻ con
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/rule/RuleCard.fxml"));
                Parent card = loader.load();

                // Đổ dữ liệu vào thẻ và thiết lập hàm callback để refresh khi xóa/sửa
                RuleCardController cardCtrl = loader.getController();
                cardCtrl.setData(rule, this::loadData);

                ruleContainer.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("Lỗi load RuleCard: " + e.getMessage());
            }
        }
    }

    // ================================================================
    // XỬ LÝ SỰ KIỆN TỪ FXML
    // ================================================================

    /**
     * Xử lý khi nhấn nút Tìm kiếm
     */
    @FXML
    void handleSearch() {
        if (masterList == null) return;

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

    /**
     * Xử lý khi nhấn nút Làm mới (🔄)
     */
    @FXML
    void handleReload() {
        if (txtSearch != null) txtSearch.clear();
        loadData();
    }

    /**
     * Mở modal thêm quy luật mới
     */
    @FXML
    void handleAddNewRule() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/rule/RuleFormModal.fxml"));
            Parent root = loader.load();

            RuleFormController ctrl = loader.getController();
            ctrl.setMode(null); // Chế độ Thêm mới (ADD)
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