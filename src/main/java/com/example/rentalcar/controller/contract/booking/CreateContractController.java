package com.example.rentalcar.controller.contract.booking;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * CreateContractController – Controller chính điều phối 4 bước tạo hợp đồng.
 *
 * Chức năng:
 *  - Giữ 1 object ContractDraft duy nhất, truyền sang từng step controller.
 *  - Điều hướng Tiếp theo / Quay lại giữa 4 bước.
 *  - Validate từng bước trước khi cho phép sang bước tiếp theo.
 *  - Bước 4 xác nhận → gọi Step4Controller.confirmAndSave().
 *
 * Đường dẫn: src/main/java/com/example/rentalcar/controller/contract/booking/CreateContractController.java
 */
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
    private int            currentStep = 1;
    private ContractDraft  draft       = new ContractDraft();

    // Giữ reference controller từng bước để validate & lưu data
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
        // Validate & lưu dữ liệu bước hiện tại vào draft trước khi tiếp
        if (!validateAndSaveCurrentStep()) return;

        if (currentStep < 4) {
            currentStep++;
            loadStep(currentStep);
        }
        // Bước 4: nút đổi thành "Xác nhận & Lưu" → xử lý trong updateStepperUI
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

            // Lấy controller và truyền draft vào
            switch (step) {
                case 1 -> {
                    step1Ctrl = loader.getController();
                    step1Ctrl.setDraft(draft);
                }
                case 2 -> {
                    step2Ctrl = loader.getController();
                    step2Ctrl.setDraft(draft);
                }
                case 3 -> {
                    step3Ctrl = loader.getController();
                    step3Ctrl.setDraft(draft);
                }
                case 4 -> {
                    step4Ctrl = loader.getController();
                    step4Ctrl.setDraft(draft);
                    // Truyền callback để Step4 đóng cửa sổ sau khi lưu thành công
                    step4Ctrl.setOnSaved(() -> closeWindow());
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
    //  VALIDATE & LƯU DỮ LIỆU TỪNG BƯỚC VÀO DRAFT
    // =========================================================
    private boolean validateAndSaveCurrentStep() {
        return switch (currentStep) {
            case 1 -> {
                if (step1Ctrl == null) yield false;
                yield step1Ctrl.validateAndSave(); // Lưu customer vào draft
            }
            case 2 -> {
                if (step2Ctrl == null) yield false;
                yield step2Ctrl.validateAndSave(); // Lưu vehicle vào draft
            }
            case 3 -> {
                if (step3Ctrl == null) yield false;
                yield step3Ctrl.validateAndSave(); // Lưu thời gian, cọc, giá vào draft
            }
            case 4 -> {
                // Bước 4: gọi confirm lưu xuống DB
                if (step4Ctrl == null) yield false;
                step4Ctrl.confirmAndSave();
                yield false; // Trả false để không tăng step nữa (cửa sổ sẽ đóng)
            }
            default -> true;
        };
    }

    // =========================================================
    //  CẬP NHẬT UI STEPPER
    // =========================================================
    private void updateStepperUI() {
        // Nút Back ẩn ở bước 1
        btnBack.setVisible(currentStep > 1);

        // Bước 4 đổi text nút Next
        btnNext.setText(currentStep == 4 ? "✅  Xác nhận & Lưu" : "Tiếp theo →");

        VBox[] boxes = { step1Box, step2Box, step3Box, step4Box };
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] == null) continue;
            boxes[i].getStyleClass().removeAll("step-active", "step-done");
            if (i + 1 < currentStep) {
                boxes[i].getStyleClass().add("step-done");
            } else if (i + 1 == currentStep) {
                boxes[i].getStyleClass().add("step-active");
            }
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