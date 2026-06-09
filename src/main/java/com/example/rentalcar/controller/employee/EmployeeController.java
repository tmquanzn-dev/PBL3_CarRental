package com.example.rentalcar.controller.employee;

import com.example.rentalcar.bll.UserBLL;
import com.example.rentalcar.models.Users;
import com.example.rentalcar.utils.AppSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class EmployeeController {

    @FXML private FlowPane employeeContainer;
    @FXML private TextField txtSearch;
    @FXML private Button btnAddEmployee;

    @FXML private Label lblTotalStaff;
    @FXML private Label lblActiveStaff;
    @FXML private Label lblLockedStaff;
    @FXML private Label lblCount;

    private final UserBLL userBLL = new UserBLL();
    private List<Users> masterList;

    @FXML
    public void initialize() {
        if (btnAddEmployee != null) {
            btnAddEmployee.setVisible(AppSession.isAdmin());
            btnAddEmployee.setManaged(AppSession.isAdmin());
        }
        loadEmployeeCards();
    }

    public void loadEmployeeCards() {
        loadFiltered(null);
    }

    private void loadFiltered(String keyword) {
        employeeContainer.getChildren().clear();
        try {
            masterList = userBLL.getAllUsers();
            List<Users> filtered = (keyword == null || keyword.isBlank())
                    ? masterList
                    : masterList.stream().filter(u ->
                            (u.getFull_name() != null
                                    && u.getFull_name().toLowerCase().contains(keyword.toLowerCase()))
                                    || u.getUsername().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();

            updateStats(masterList);
            if (lblCount != null)
                lblCount.setText(filtered.size() + " nhân viên");

            for (Users user : filtered) {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/views/employee/EmployeeCard.fxml"));
                    Parent card = loader.load();

                    EmployeeCardController ctrl = loader.getController();
                    ctrl.setData(user);
                    ctrl.setOnRefresh(this::loadEmployeeCards);

                    employeeContainer.getChildren().add(card);
                } catch (Exception e) {
                    System.err.println("Lỗi render card: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            showError("Lỗi tải danh sách nhân viên: " + e.getMessage());
        }
    }

    private void updateStats(List<Users> list) {
        if (list == null) return;
        long total  = list.size();
        long active = list.stream().filter(Users::isIs_active).count();
        long locked = total - active;

        if (lblTotalStaff  != null)
            lblTotalStaff.setText(String.valueOf(total));
        if (lblActiveStaff != null)
            lblActiveStaff.setText(String.valueOf(active));
        if (lblLockedStaff != null)
            lblLockedStaff.setText(String.valueOf(locked));
    }

    @FXML
    void handleSearch(ActionEvent event) {
        String keyword = txtSearch != null ? txtSearch.getText().trim() : "";
        loadFiltered(keyword);
    }

    @FXML
    void handleReload(ActionEvent event) {
        if (txtSearch != null)
            txtSearch.clear();
        loadEmployeeCards();
    }

    @FXML
    void showAddEmployeeModal() {
        if (!AppSession.isAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/employee/AddEmployeeModal.fxml"));
            Parent root = loader.load();

            AddEmployeeController ctrl = loader.getController();
            ctrl.setOnSaved(this::loadEmployeeCards);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Thêm nhân viên mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (Exception e) {
            showError("Lỗi mở form thêm nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Lỗi"); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}