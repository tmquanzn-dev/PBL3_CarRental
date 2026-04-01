package com.example.rentalcar.controller.contract.booking;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class CreateContractController {
    @FXML private StackPane contentArea; // Cái bụng chứa ruột
    @FXML private Button btnBack, btnNext;
    @FXML private VBox step1Box, step2Box, step3Box, step4Box; // 4 cái cục Stepper
    @FXML private Label lblStep1, lblStep2, lblStep3, lblStep4; // 4 cái vòng tròn số

    private int currentStep = 1;

    @FXML
    public void initialize() {
        loadStep(1); // mới mở lên thì hiện bước 1 luôn
    }

    private void loadStep(int step) {
        try {
            String fxmlPath = "/views/create_contract/Step" + step + "_Content.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            contentArea.getChildren().setAll(root);
            updateStepperUI();
        } catch (IOException e) {
            System.err.println("Lỗi load ruột bước " + step + ": " + e.getMessage());
        }
    }

    @FXML
    void handleNext(ActionEvent event) {
        if (currentStep < 4) {
            currentStep++;
            loadStep(currentStep);
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        if (currentStep > 1) {
            currentStep--;
            loadStep(currentStep);
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
    private void updateStepperUI() {
        btnBack.setVisible(currentStep > 1);
        btnNext.setText(currentStep == 4 ? "Xác nhận & In" : "Tiếp theo →");

        VBox[] boxes = {step1Box, step2Box, step3Box, step4Box};
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] != null) { // Kiểm tra null cho chắc chắn
                boxes[i].getStyleClass().removeAll("step-active", "step-done");
                if (i + 1 < currentStep) {
                    boxes[i].getStyleClass().add("step-done");
                } else if (i + 1 == currentStep) {
                    boxes[i].getStyleClass().add("step-active");
                }
            }
        }
    }
}