package com.example.rentalcar.controller.employee;

import com.example.rentalcar.bll.UserBLL;
import com.example.rentalcar.models.Users;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

import java.util.List;

public class EmployeeController {

    @FXML private FlowPane employeeContainer; //

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

                // Đổ dữ liệu vào card
                cardController.setData(user);

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