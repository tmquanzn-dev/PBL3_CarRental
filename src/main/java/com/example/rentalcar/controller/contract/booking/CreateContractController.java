package com.example.rentalcar.controller.contract.booking;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class CreateContractController {

    // =========================================================
    //  FXML
    // =========================================================
    @FXML private StackPane contentArea;
    @FXML private Button    btnBack;
    @FXML private Button    btnNext;

    // Stepper boxes
    @FXML private VBox step1Box;
    @FXML private VBox step2Box;
    @FXML private VBox step3Box;
    @FXML private VBox step4Box;

    // =========================================================
    //  STATE
    // =========================================================
    private int           currentStep = 1;
    private ContractDraft draft       = new ContractDraft();

    private Step1Controller step1Ctrl;
    private Step2Controller step2Ctrl;
    private Step3Controller step3Ctrl;
    private Step4Controller step4Ctrl;

    // =========================================================
    //  INITIALIZE
    // =========================================================
    @FXML
    public void initialize() {
        loadStep(1);
    }

    // =========================================================
    //  ĐIỀU HƯỚNG
    // =========================================================
    @FXML
    void handleNext(ActionEvent event) {
        if (!validateAndSaveCurrentStep()) return;

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
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // =========================================================
    //  LOAD STEP
    // =========================================================
    private void loadStep(int step) {
        try {
            String path = "/views/create_contract/Step" + step + "_Content.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            switch (step) {
                case 1 -> { step1Ctrl = loader.getController(); step1Ctrl.setDraft(draft); }
                case 2 -> { step2Ctrl = loader.getController(); step2Ctrl.setDraft(draft); }
                case 3 -> { step3Ctrl = loader.getController(); step3Ctrl.setDraft(draft); }
                case 4 -> {
                    step4Ctrl = loader.getController();
                    step4Ctrl.setDraft(draft);
                    step4Ctrl.setOnSaved(this::closeWindow);
                }
            }

            contentArea.getChildren().setAll(root);
            updateStepperUI();

        } catch (IOException e) {
            System.err.println("Lỗi load Step " + step + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================
    //  VALIDATE & LƯU
    // =========================================================
    private boolean validateAndSaveCurrentStep() {
        return switch (currentStep) {
            case 1 -> step1Ctrl != null && step1Ctrl.validateAndSave();
            case 2 -> step2Ctrl != null && step2Ctrl.validateAndSave();
            case 3 -> step3Ctrl != null && step3Ctrl.validateAndSave();
            case 4 -> {
                if (step4Ctrl != null) step4Ctrl.confirmAndSave();
                yield false;
            }
            default -> true;
        };
    }

    // =========================================================
    //  CẬP NHẬT STEPPER UI
    // =========================================================
    private void updateStepperUI() {
        // Nút Back: ẩn ở bước 1
        if (btnBack != null) {
            btnBack.setVisible(currentStep > 1);
            btnBack.setManaged(currentStep > 1);
        }

        // Bước 4 đổi text nút Next
        if (btnNext != null) {
            if (currentStep == 4) {
                btnNext.setText("✅  Xác nhận & Lưu");
                btnNext.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white;" +
                        " -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;" +
                        " -fx-font-size: 14px;" +
                        " -fx-effect: dropshadow(three-pass-box,rgba(22,163,74,0.4),10,0,0,3);");
            } else {
                btnNext.setText("Tiếp theo →");
                btnNext.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;" +
                        " -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;" +
                        " -fx-font-size: 14px;" +
                        " -fx-effect: dropshadow(three-pass-box,rgba(37,99,235,0.4),10,0,0,3);");
            }
        }

        // Cập nhật màu từng step box
        updateStepBox(step1Box, 1);
        updateStepBox(step2Box, 2);
        updateStepBox(step3Box, 3);
        updateStepBox(step4Box, 4);
    }

    private void updateStepBox(VBox box, int stepNum) {
        if (box == null) return;

        // Lấy Label số step (phần tử đầu trong HBox con đầu tiên)
        try {
            HBox innerHBox = (HBox) box.getChildren().get(0);
            Label circleLabel = (Label) innerHBox.getChildren().get(0);
            Label textLabel   = (Label) innerHBox.getChildren().get(1);

            if (stepNum < currentStep) {
                // Đã hoàn thành
                circleLabel.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white;" +
                        " -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 50;" +
                        " -fx-min-width: 28; -fx-min-height: 28; -fx-alignment: center;");
                circleLabel.setText("✓");
                textLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #16a34a; -fx-font-weight: bold;");
            } else if (stepNum == currentStep) {
                // Đang ở bước này
                circleLabel.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;" +
                        " -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 50;" +
                        " -fx-min-width: 28; -fx-min-height: 28; -fx-alignment: center;");
                circleLabel.setText(String.valueOf(stepNum));
                textLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2563eb; -fx-font-weight: bold;");
            } else {
                // Chưa tới
                circleLabel.setStyle("-fx-background-color: #9ca3af; -fx-text-fill: white;" +
                        " -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 50;" +
                        " -fx-min-width: 28; -fx-min-height: 28; -fx-alignment: center;");
                circleLabel.setText(String.valueOf(stepNum));
                textLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af;");
            }
        } catch (Exception ignored) {
            // Bỏ qua nếu cấu trúc không khớp
        }
    }

    // =========================================================
    //  HELPER
    // =========================================================
    private void closeWindow() {
        Stage stage = (Stage) contentArea.getScene().getWindow();
        stage.close();
    }

    static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    static void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}