package com.example.rentalcar.controller.employee;

import com.example.rentalcar.bll.UserBLL;
import com.example.rentalcar.models.Users;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.TilePane; // DÙNG TILEPANE ĐỂ KHÓA KÍCH THƯỚC

import java.util.List;

public class EmployeeController {
    // Đã đổi thành TilePane cho khớp với FXML
    @FXML private TilePane employeeContainer;

    private final UserBLL userBLL = new UserBLL();

    @FXML
    public void initialize() {
        loadEmployeeCards();
    }

    public void loadEmployeeCards() {
        try {
            employeeContainer.getChildren().clear();
            List<Users> usersList = userBLL.getAllUsers();

            for (Users user : usersList) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/employee/EmployeeCard.fxml"));
                Parent card = loader.load();

                EmployeeCardController cardController = loader.getController();

                Platform.runLater(() -> {
                    cardController.setData(user);
                });

                employeeContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void showAddEmployeeModal() {
        System.out.println("Dang them nhan vien");
    }
}